# 自定义配置

## ConfigResponder

ANTE 提供 `ConfigResponder` 类来表示配置的响应器，存储配置的信息。

- `new ConfigResponder(key: String, name: Component, defaultValue: String, transformer: Function<String, String>, errorSupplier: Function<String, Optional<Component>>, saveConsumer: Consumer<String>, consumer: Consumer<TextFieldBuilder>)`

    创建一个配置响应器。
    `key`: 配置项的标识。
    `name`: 配置项的名称。
    `defaultValue`: 配置项的默认值。
    `transformer`: 配置项的转换器，显示的值是保存的值经过转换器转换后的结果。
    `errorSupplier`: 配置项的错误提示，用于提示用户输入错误。
    `saveConsumer`: 配置项的保存函数，您只需要在此函数中写入您的处理逻辑即可。
    `consumer`: 对 `TextFieldBuilder` 的更多操作。

`ConfigResponder` 含有以上属性，除了 `key` 以外，其他属性您可以随时修改。

## ClientConfig

为了让资源包可以在游戏中调整设置，ANTE向Js环境提供了 `ClientConfig` 类，可以注册和获取配置，配置文件会被保存在客户端。注册后的配置将会出现在 由 `/mtrnte config` 命令调出的设置界面的最下方。

- `static ClientConfig.register(response: ConfigResponder): void `

    添加一个响应器。

- `static get(key: String): String`

    `String` key: 配置项的标识。

- `static save(): void`

    保存所有配置项到本地文件。

### 示例

```javascript
// ···
// 如果值不是 true 或 false，则提示用户输入错误
const errorSupplier = (str) => {
    if (str == "true" || str == "false") return java.util.Optional.empty();
    else return java.util.Optional.of(ComponentUtil.translatable("text.aph.config.error"));
}

ClientConfig.register("myConfig", ComponentUtil.translatable("text.aph.config.myConfig"), "true", value => value, errorSupplier, str => {});

function render(ctx, state, entity) {
    // ···
    const config = ClientConfig.get("myConfig");// String
    ctx.setDebugInfo("myConfig: " + config)
    // ···
}
// ···
```