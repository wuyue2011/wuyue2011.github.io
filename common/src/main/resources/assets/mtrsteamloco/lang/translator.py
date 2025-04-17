# -*- coding: utf-8 -*-
import requests
import random
import json
import re
from hashlib import md5
from tqdm import tqdm
import os
import time

# 配置路径
CONFIG_PATH = r"D:\bdapi.json"
SOURCE_FILE = "zh_cn.json"
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

# 语言映射配置
LANG_MAP = {
    'de_de': 'de',
    'en_us': 'en',
    'ja_jp': 'jp',
    'pt_pt': 'pt',
    'zh_hk': 'cht',
    'zh_tw': 'cht',
    'fr_fr': 'fra',
    'es_es': 'spa',
    'it_it': 'it',
    'ru_ru': 'ru',
    'ko_kr': 'kor'
}

class BaiduTranslator:
    def __init__(self, appid, secret_key):
        self.appid = appid
        self.secret_key = secret_key
        self.endpoint = 'http://api.fanyi.baidu.com/api/trans/vip/translate'
        self.retry_limit = 3
        self.delay = 1

    def _make_md5(self, s):
        return md5(s.encode('utf-8')).hexdigest()

    def _replace_specials(self, text):
        # 保护特殊格式
        replacements = {
            r'%s': ' MTR_PLACEHOLDER_S ',
            r'§[a-f0-9]': lambda m: f' MTR_COLOR_{m.group(0)[1:]} ',
            r'https?://\S+': lambda m: f' MTR_URL_{hash(m.group(0))} '
        }
        
        protected = text
        for pattern, repl in replacements.items():
            protected = re.sub(pattern, repl, protected, flags=re.IGNORECASE)
        
        return protected, replacements

    def _restore_specials(self, text, replacements):
        # 恢复特殊格式
        restored = text
        restore_map = {
            'MTR_PLACEHOLDER_S': '%s',
            r'MTR_COLOR_([a-f0-9])': r'§\1',
            r'MTR_URL_(\d+)': lambda m: next((k for k, v in replacements[2].items() 
                                          if str(hash(k)) == m.group(1)), '')
        }
        
        for i, (pattern, repl) in enumerate(restore_map.items()):
            if i == 2:  # URL 处理
                for url_placeholder in re.findall(r'MTR_URL_\d+', restored):
                    hash_val = url_placeholder.split('_')[-1]
                    for original_url in replacements[2]:
                        if str(hash(original_url)) == hash_val:
                            restored = restored.replace(url_placeholder, original_url)
                            break
            else:
                restored = re.sub(pattern, repl, restored, flags=re.IGNORECASE)
        
        return restored

    def translate(self, query, to_lang):
        salt = random.randint(32768, 65536)
        sign = self._make_md5(self.appid + query + str(salt) + self.secret_key)
        
        params = {
            'q': query,
            'from': 'zh',
            'to': to_lang,
            'appid': self.appid,
            'salt': salt,
            'sign': sign
        }

        for _ in range(self.retry_limit):
            try:
                response = requests.post(self.endpoint, params=params, timeout=10)
                result = response.json()
                if 'error_code' in result:
                    raise Exception(f"API Error {result['error_code']}: {result['error_msg']}")
                return ' '.join([item['dst'] for item in result['trans_result']])
            except Exception as e:
                print(f"Translation failed: {str(e)}, retrying...")
                time.sleep(self.delay)
        
        raise Exception("Translation failed after multiple retries")

class TranslationProcessor:
    def __init__(self, translator):
        self.translator = translator
        self.total_items = 0
        self.processed_items = 0

    def process_file(self, src_data, target_lang):
        translated = {}
        target_code = LANG_MAP[target_lang]
        
        with tqdm(total=len(src_data), desc=f"Translating {target_lang}", unit="item") as pbar:
            for key, value in src_data.items():
                protected_text, replacements = self.translator._replace_specials(value)
                
                try:
                    translated_text = self.translator.translate(protected_text, target_code)
                    final_text = self.translator._restore_specials(translated_text, replacements)
                except Exception as e:
                    print(f"Error translating {key}: {str(e)}")
                    final_text = value  # 保留原文作为后备
                
                translated[key] = final_text
                pbar.update(1)
                self.processed_items += 1
        
        return translated

def main():
    # 加载配置
    with open(CONFIG_PATH, 'r', encoding='utf-8') as f:
        config = json.load(f)
    
    # 初始化翻译器
    translator = BaiduTranslator(config['appid'], config['secret_key'])
    processor = TranslationProcessor(translator)
    
    # 加载源文件
    with open(os.path.join(BASE_DIR, SOURCE_FILE), 'r', encoding='utf-8') as f:
        source_data = json.load(f)
    
    # 计算总工作量
    total_langs = len(LANG_MAP)
    processor.total_items = len(source_data) * total_langs
    
    # 全局进度条
    with tqdm(total=total_langs, desc="Overall Progress", unit="lang") as global_pbar:
        for target_lang, target_code in LANG_MAP.items():
            if target_lang == 'zh_cn':  # 跳过简体中文
                continue
            
            # 翻译处理
            translated_data = processor.process_file(source_data, target_lang)
            
            # 保存结果
            output_path = os.path.join(BASE_DIR, f"{target_lang}.json")
            with open(output_path, 'w', encoding='utf-8') as f:
                json.dump(translated_data, f, ensure_ascii=False, indent=4)
            
            global_pbar.update(1)

if __name__ == "__main__":
    main()