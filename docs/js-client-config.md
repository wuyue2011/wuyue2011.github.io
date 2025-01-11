# ClientConfig

为了让资源包可以在游戏中调整设置，ANTE向Js环境提供了 `ClientConfig` 类，可以注册和获取配置，配置文件会被保存在客户端。

- `static ClientConfig.register(key: String, name: Component, defaultValue: String, transformer: Function<String, String>, errorSupplier: Function<String, Optional<Component>>, saveConsumer: Consumer<String>): void `

    `String` key: 配置项的标识
    `Component` name: 显示的标签名称
    `String` defaultValue: 默认值
    `Function<String, String>` transformer: 转换存储的值为显示的值
    `Function<String, Optional<Component>>` errorSupplier: 错误提示
    `Consumer<String>` saveConsumer: 保存配置项的回调

- `static get(key: String): String`

    `String` key: 配置项的标识

- `static save(): void`

    保存所有配置项到本地文件

## 示例

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