# 最新功能

有些功能最近被加入，可能会有变动。

## JavaScript 环境






### ClientConfig

为可以在客户端调整设置，我向JS环境中添加了ClientConfig类，可以注册和获取配置，配置文件会被保存在客户端。

- `static void register(String key, Component name, String defaultValue, Function<String, String> transformer, Function<String, Optional<Component>> errorSupplier, Consumer<String> saveConsumer) `

    `String` key: 配置项的标识
    `Component` name: 显示的标签名称
    `String` defaultValue: 默认值
    `Function<String, String>` transformer: 转换存储的值为显示的值
    `Function<String, Optional<Component>>` errorSupplier: 错误提示
    `Consumer<String>` saveConsumer: 保存配置项的回调

- `static String get(String key)`

    `String` key: 配置项的标识

- `static void save()`

保存所有配置项到本地文件

#### 示例

```javascript
// ···

const errorSupplier = (str) => {
    if (str == "true" || str == "false") return java.util.Optional.empty();
    else return java.util.Optional.of(ComponentUtil.translatable("text.aph.config.error"));
}
let times = 0;

ClientConfig.register("myConfig", ComponentUtil.translatable("text.aph.config.myConfig"), "true", value => value, errorSupplier, str => times++);

function render(ctx, state, entity) {
    // ···
    const config = ClientConfig.get("myConfig");// String
    // ···
}
// ···
```

### EyeCandy

请查看 [JavaScript 装饰物件相关](https://aphrodite281.github.io/mtr-ante/#/js-eyecandy)