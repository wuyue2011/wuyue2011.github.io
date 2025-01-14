# 自定义配置

## ConfigResponder

ANTE 提供 `ConfigResponder` 类来表示配置的响应器，存储配置的信息。更进一步的，请查看[源代码-ConfigResponder.java](https://github.com/aphrodite281/mtr-ante/blob/alpha/common/src/main/java/cn/zbx1425/mtrsteamloco/data/ConfigResponder.java)

- `new ConfigResponder(key: String, name: Component, defaultValue: String)`

    创建一个配置响应器。

- `new ConfigResponder(key: String, name: Component, defaultValue: String, transformer: Function<String, String>, errorSupplier: Function<String, Optional<Component>>, saveConsumer: Consumer<String>, tooltipSupplier: Function<String, Optional<List<Component>>>, requireRestart: boolean)`

    创建一个配置响应器。

包含以下属性以及对应方法：

| 属性 | 方法 | 说明 |
| ------------- | ------------- | ------------- |
| `final ConfigResponder.key: String` | 无 | 配置项的标识。无法修改 |
| `ConfigResponder.name: Component` | `ConfigResponder.setName(name: Component): ConfigResponder` | 配置项的名称 |
| `ConfigResponder.defaultValue: String` | `ConfigResponder.setDefaultValue(defaultValue: String): ConfigResponder` | 配置项的默认值 |
| `ConfigResponder.transformer: Function<String, String>` | `ConfigResponder.setTransformer(transformer: Function<String, String>): ConfigResponder` | 配置项的转换器 |
| `ConfigResponder.errorSupplier: Function<String, Optional<Component>>` | `ConfigResponder.setErrorSupplier(errorSupplier: Function<String, Optional<Component>>): ConfigResponder` | 配置项的错误提示 |
| `ConfigResponder.saveConsumer: Consumer<String>` | `ConfigResponder.setSaveConsumer(saveConsumer: Consumer<String>): ConfigResponder` | 配置项的保存函数 |
| `ConfigResponder.tooltipSupplier: Function<String, Optional<Component[]>>` | `ConfigResponder.setTooltipSupplier(tooltipSupplier: Function<String, Optional<List<Component>>>): ConfigResponder` | 配置项的提示信息 |
| `ConfigResponder.requireRestart: boolean` | `ConfigResponder.setRequireRestart(requireRestart: boolean): ConfigResponder` | 配置项是否需要重启游戏 |

上文中的 [Optional](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html) 是 Java 8 引入的类，用来表示一个值可能为空。您可以使用 `importClass(java.util.Optional)` 、 `importClass(java.util)` 引入该类或直接使用 `java.util.Optional` 来表示Optional。
`tooltipSupplier` 变量的返回值应该是 `Optional<Component[]>` 而不是 `Optional<List<Component>>` , 由于在 JavaScirpt 环境中得到 `Component[]` 太过麻烦，所以这里提供了 `ConfigResponder.setErrorSupplier(errorSupplier: Function<String, Optional<List<Component>>>)` 方法来代替，您可以直接将 [JavaScirpt的数组](https://github.com/aphrodite281/mtr-ante/blob/alpha/rhino/src/main/java/vendor/cn/zbx1425/mtrsteamloco/org/mozilla/javascript/NativeArray.java)传入(因为它实现了List接口)
最后，本类支持链式调用。

## ClientConfig

为了让资源包可以在游戏中调整设置，ANTE向Js环境提供了 `ClientConfig` 类，可以注册和获取配置，配置文件会被保存在客户端。注册后的配置将会出现在 由 `/mtrnte config` 命令调出的设置界面的最下方。更进一步的，请查看[源代码-ClientConfig.java](https://github.com/aphrodite281/mtr-ante/blob/alpha/common/src/main/java/cn/zbx1425/mtrsteamloco/ClientConfig.java)

- `static ClientConfig.register(response: ConfigResponder): void `

    添加一个响应器。

- `static get(key: String): String`

    `String` key: 配置项的标识。

- `static save(): void`

    保存所有配置项到本地文件。

`ClientConfig`

## 示例

### 使用长构造函数

```javascript
const configKey = "myConfig";
// ···
// 如果值不是 true 或 false，则提示用户输入错误
const errorSupplier = (str) => {
    if (str == "true" || str == "false") return java.util.Optional.empty();
    else return java.util.Optional.of(ComponentUtil.translatable("text.aph.config.error"));
}

const res = new ConfigResponder(configKey, 
    ComponentUtil.translatable("text.aph.config.myConfig"), "true", 
    value => value, errorSupplier, str => {}, 
    str => java.util.Optional.empty(), false);

ClientConfig.register(res);

function render(ctx, state, entity) {
    // ···
    let config = ClientConfig.get(configKey);// String
    ctx.setDebugInfo("myConfig: " + config)
    // ···
}
// ···
```

### 使用链式调用

```javascript
const configKey = "myConfig";
const errorSupplier = (str) => {
    if (str == "true" || str == "false") return java.util.Optional.empty();
    else return java.util.Optional.of(ComponentUtil.translatable("text.aph.config.error"));
}

const res = new ConfigResponder(configKey, 
    ComponentUtil.translatable("text.aph.config.myConfig"), "true")
    .setErrorSupplier(errorSupplier);
    .setTooltipSupplier(str => java.util.Optional.of([ComponentUtil.translatable("text.aph.config.tooltip")]));

ClientConfig.register(res);
```