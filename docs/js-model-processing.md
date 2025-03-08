# JavaScript 模型处理

## 介绍

在 NTE 中一种较为基础的处理模型的方法是，模型文件首先可以加载为 `RawModel`，接下来可以随意对他进行一些处理，然后要进行一个上传过程得到 `ModelCluster` 或 `DynamicModelHolder`，最后在渲染时将 `ModelCluster` 或 `DynamicModelHolder` 交给 NTE 显示。

如果不使用模型文件而是程序地添加顶点数据，可以使用 `RawMeshBuilder` 构建 `RawMesh` 以及 `RawModel` ，接下来可以对他进行一些处理，然后上传得到 `ModelCluster` 或 `DynamicModelHolder`。


NTE 使用这些类加载和处理模型：


- `Face`：面，存储了顶点的索引

- `Vertex`：顶点，存储了位置、法线、颜色、UV坐标、亮度

- `VertAttrType` ：顶点属性类型，存储了顶点属性

- `VertAttrSrc` ：顶点属性类型，存储了顶点内容

- `VertAttrMapping`：顶点属性映射，存储了一些 VertAttrType 和 VertAttrSrc 同时还有缓冲区信息




- `VertAttrState`：顶点属性状态，存储了坐标、法线、颜色、UV坐标、叠加UV坐标、光照UV坐标、法线方向、矩阵模型

- `MaterialProp`: 材质属性，存储了所要使用的贴图路径、着色器种类、材质颜色（在 VertAttrState 内）等。


- `VertArray`：相当于上传之后的 `RawMesh`，是绑定了对应的 VBO 和 EBO 的 VAO 对象的引用。


- `VertArrays`：相当于上传之后的 `RawModel`，包含多个 `VertArray`。


- `RawMeshBuilder`：原始网格构造器，用于构造 RawMesh 。其包含一个 RawMesh ,一个面顶点数量和一个 Vertex 存储临时顶点信息。

- `RawMesh`：原始网格类，包含了一个 MaterialProp ，一个  Face 数组 faces、以一个 Vertex 数组 vertices。

- `RawModel`：原始网格模型，含有来源和一个 meshList(HashMap<MaterialProp, RawMesh>)，存储了多个 由 MaterialProp 和 RawMesh 组成的键值对。

- `ModelCluster`：实际上上传 `RawModel` 所得到的，以及渲染相关函数所接受的类型。分别存储了透明和不透明部分的 `RawModel` 和 `VertArray`，以便于特定处理。


- `DynamicModelHolder`：动态模型容器，以解决因调用不在主线程而无法在函数内通过 ModelManager 上传 RawModel 为 ModelCluster 的问题，包含一个 ModelCluster。


- `ModelManager`：模型管理器，用于加载模型或上传模型如加载 OBJ 为 RawModel 或上传 RawModel 为 ModelCluster。


下面将详细介绍这些类及其用法，末尾有一些示例代码，也可以参考其他示例代码。



## ModelManager

NTE 提供了一个方便的模型管理器方便加载模型等，打包了一些常用的加载模型的函数。

源码为 ModelManager.java 文件。

- `static ModelManager.loadRawModel(Resources.manager(), path: ResourceLocation, null): RawModel`

  将一个模型整体地加载为一个 RawModel。

- `static ModelManager.loadPartedRawModel(Resources.manager(), path: ResourceLocation, null): Map<String, RawModel>`

  将一个模型的每个分组分别加载成各个 RawModel，返回一个 Java Map。

- `static ModelManager.uploadVertArrays(rawModel: RawModel): ModelCluster`

  把一个 RawModel 上传到显存，返回上传好的 ModelCluster。注意由于线程问题，不能在 create/render/dispose 中调用它。如需动态更新模型需使用 `DynamicModelHolder`。


如果需要在函数内（如create、render、dispose或在其中调用的函数）上传 RawModel 为 ModelCluster 需要使用 DynamicModelHolder。


## RawMeshBuilder 

NTE 有 RawMeshBuilder 类，用于在程序中创建 RawMesh 。该类在DisplayHelper类中有用到，用于在程序中创建网格对象。
RawMeshBuilder 支持链式调用，可以将一个操作跟到另一个操作后面，在下面的示例文件中可以看到具体的用法。

源码为 RawMeshBuilder.java 文件。

下面是一些函数可以使用

- ` new RawMeshBuilder(faceSize: int, renderType: String, texture: ResourceLocation) `

    创建一个 RawMeshBuilder 对象。

- ` RawMeshBuilder.reset(): RawMeshBuilder `

    重置此 RawMeshBuilder 对象，并返回此 RawMeshBuilder 对象。

- ` RawMeshBuilder.vertex(position: Vector3f): RawMeshBuilder `
- ` RawMeshBuilder.vertex(x: double, y: double, z: double): RawMeshBuilder `

    添加一个顶点到此 RawMeshBuilder 对象，并返回此 RawMeshBuilder 对象。

- ` RawMeshBuilder.normal(x: double, y: double, z: double): RawMeshBuilder `

    设定顶点的法向量，并返回此 RawMeshBuilder 对象。


- ` RawMeshBuilder.uv(x: float, y: float): RawMeshBuilder `

    设定顶点的 UV 坐标，并返回此 RawMeshBuilder 对象。


- ` RawMeshBuilder.endVertex(): RawMeshBuilder `

    结束一个顶点的定义，并返回此 RawMeshBuilder 对象。

- ` RawMeshBuilder.color(red : int, green : int, blue : int, alpha : int): RawMeshBuilder `

    设置 RGBA 颜色值到此 RawMeshBuilder 对象的材质中，并返回此 RawMeshBuilder 对象。

- ` RawMeshBuilder.lightMapUV(x: float, y: float): RawMeshBuilder `

    设置 lightMapUV 值到此 RawMeshBuilder 对象的材质中，并返回此 RawMeshBuilder 对象。

- ` RawMeshBuilder.setNewDefaultVertex(texture: ResourceLocation): RawMeshBuilder `

    设置一个新的默认顶点到此 RawMeshBuilder 对象，并返回此 RawMeshBuilder 对象。(提一嘴在 Build #619 前此方法不会返回 RawMeshBuilder )




## RawModel

NTE 使用 RawModel 来初加载模型，它可以通过 ModelManager 快捷的加载 OBJ 文件、CSV 文件、 NMB 文件等（NMB 文件是 NTE 的字节模型格式，全称为"NTE Model Binary"，不要想歪）或通过其提供的方法 添加 RawMesh 对象为 RawModel 。

每一个 RawModel 类内含有 sourceLocation(ResourceLocation) 和 meshList(HashMap<MaterialProp, RawMesh>) 两个成员变量。可以直接通过RawModel.sourceLocation 或 RawModel.meshList 来访问或更改。

sourceLocation 变量用于记录来源，如果两个的 RawModel 的来源一样，那么这两个 RawModel 实际上都是同一个 RawModel ，使用 RawModel.copy() 或RawModel.copyForMaterialChanges() 新的RawModel 都会记录此来源为原 RawModel 的来源 ，如果你不希望新对的 RawModel 的操作会影响原 RawModel ，可以在.copy()或.copyForMaterialChanges() 后设置 newRawModel.sourceLocation = null 来断开与原 RawModel 的关联，之后对两个模型的操作不会影响另一个 RawModel 。

源码为 RawModel.java 文件。

RawModel 类提供了以下方法：

| 函数                                                         | 说明                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| `new RawModel()`| 创建一个空的 RawModel 对象。 |
| `RawModel.append(other: RawModel): void`                     | 将另一个 RawModel 合并到这个 RawModel 里面。                 |
| `RawModel.append(other: RawMesh): void`                      | 将一个 RawMesh 合并到这个 RawModel 里面。                    |
| `RawModel.append(other: Collection<RawMesh>): void`          | 将一些 RawMesh 合并到这个 RawModel 里                      |
| `RawModel.applyMatrix(transform: atrix4f): void`             | 用一个矩阵来变换模型里的所有顶点。                           |
| `RawModel.applyTranslation(x: float, y: float, z: float): void` | 平移模型里的所有顶点。                                       |
| `RawModel.applyRotation(direction: Vector3f, angle: float): void` | 以原点为中心绕一个轴旋转模型里的所有顶点。角度采用角度制。   |
| `RawModel.applyScale(x: float, y: float, z: float): void`    | 缩放模型。                                                   |
| `RawModel.applyMirror(vx: boolean, vy: boolean, vz: boolean, nx: boolean, ny: boolean, nz: boolean): void` | 镜面翻转模型。<br />六个布尔值，前三个为要否变换顶点，后三个为要否翻转法线方向。 |
| `RawModel.applyUVMirror(u: boolean, v: boolean): void`       | 反转 UV 方向。最终需要 V 正方向向下，所以导入 Blockbench 或 Blender 模型时需 `rawModel.applyUVMirror(false, true)`。 |
| `RawModel.replaceTexture(oldFileName: String, path: ResourceLocation): void` | 把所有文件名为 `oldFileName` 的贴图替换为 `resourceLocation` 所指定的贴图。 |
| `RawModel.replaceAllTexture(path: ResourceLocation): void`   | 把所有贴图替换为 `resourceLocation` 所指定的贴图。           |
| `RawModel.copy(): RawModel`                                  | 复制模型的材质和顶点数据为新模型。                         |
| `RawModel.copyForMaterialChanges(): RawModel`                | 复制模型的材质为新模型，但和原先的模型共用一组顶点数据。     |
|`RawModel.setAllRenderType(renderType: String): void`	|设置所有材质的渲染类型。如"exterior"|
|`RawModel.generateNormals(): void`	|生成法线。|
|`RawModel.distinct(): void`	|精简模型，去除完全重复的面。|
|`RawModel.triangulate(): void`	|三角化面，可以解决一些由于组成面的顶点不在同一平面导致的渲染问题。|
|`RawModel.applyShear(direction: Vector3f, shear: Vector3f, ratio: float): void`	|应用切变变换。|
|`RawModel.clearAttrState(attrType: VertAttrType): void`	|删除其中指定的顶点属性|
|`RawModel.copyForMaterialChanges(): RawModel`	|创建当前模型的副本，但只复制材质属性，顶点数据与原模型共享。|



## ModelCluster

模型上传之后就不能再修改顶点数据和渲染阶段了，不过也还可以替换贴图。因此一个模型需要多次替换贴图时，可以先上传再替换，避免每次都替换后再上传产生的不必要的上传操作。

其中含有 uploadedOpaqueParts（VertArrays）、opaqueParts（RawModel）、uploadedTranslucentParts（VertArrays）、translucentParts（RawModel）几个成员变量。他们都带有final修饰符，不能被二次修改为其他值，但可以被读取并使用其的函数。RawModel 可以参考上面的 RawModel 部分，VertArrays 可以获得MatreialProp ，可以开发更加基本的渲染功能，详见下面MaterialProp。

下面是一些函数可以使用：

| 函数                                                         | 说明                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| `ModelCluster.replaceTexture(oldFileName: String, path: ResourceLocation): void` | 把所有文件名为 `oldFileName` 字符串的贴图替换为 `resourceLocation` 所指定的贴图。 |
| `ModelCluster.replaceAllTexture(path: ResourceLocation): void` | 把所有贴图替换为 `resourceLocation` 所指定的贴图。           |
| `ModelCluster.copyForMaterialChanges(): ModelCluster`        | 复制模型的材质为新模型，但和原先的模型共用一组顶点数据。     |
|`ModelCluster.close(): void`	|关闭此 ModelCluster 实例|

下面还有一些成员变量可以获取：

| 方法                                                         | 说明                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| `ModelCluster.uploadedOpaqueParts: VertArrays` | 已上传的不透明部分的 VertArrays。 |
| `ModelCluster.opaqueParts: RawModel` | 不透明部分的 RawModel。 |
| `ModelCluster.uploadedTranslucentParts: VertArrays` | 已上传的透明部分的 VertArrays。 |
| `ModelCluster.translucentParts: RawModel` | 透明部分的 RawModel。 |


## VertArrays

VertArrays 可以从 ModelCluster 获得，其包含有以下方法：

- `VertArrays.meshList: ArrayList<VertArray>`
    
    获取 VertArrays 中的各个不同材质所对应的 VertArray。


- `VertArrays.replaceTexture(String oldTexture, ResourceLocation newTexture)`

    所有文件名为 `oldFileName` 字符串的贴图替换为 `resourceLocation` 所指定的贴图。

- `VertArrays.replaceAllTexture(ResourceLocation newTexture)`

    把所有贴图替换为 `resourceLocation` 所指定的贴图。



## VertArray

VertArray 可以从 VertArrays 获得，由于有些函数较为深入底层，这里只介绍一些常用的函数：

- `VertArray.materialProp: MaterialProp`

    获取 VertArray 的 MaterialProp 。

- `VertArray.mapping: VertAttrMapping`
    
    获取 VertArray 的 VertAttrMapping 。



## MaterialProp

MaterialProp 可以从 VertArray 获得，其包含有以下方法：

- `MaterialProp.shaderName: String`

    获取材质名。

- `MaterialProp.texture: ResourceLocation`
    
    获取材质使用的贴图路径。

- `MaterialProp.translucent: boolean`

    获取材质是否透明。

- `MaterialProp.writeDepthBuf: boolean`
    
    获取材质是否写入深度缓冲区。

- `MaterialProp.billboard: boolean`：

    获取材质是否始终面向摄像机。

- `MaterialProp.attrState: VertAttrState`

    获取 VertAttrState 。



## VertAttrState

VertAttrState 可以从 MaterialProp 获得。材质颜色在此处设定，其中设定的颜色会与贴图上的颜色相乘。


- `VertAttrState.color: int`

    获取颜色。(格式为RRGGBBAA)

- `VertAttrState.setColor(rgba: int): void`
    或
- `VertAttrState.setColor(red: int, green: int, blue: int, alpha: int): void`

    设置颜色。每项为 0~255 的整数。



## DynamicModelHolder

为了解决 ModelManager 无法在函数内上传 RawModel 为 ModelCluster 这一问题 NTE 添加了 DynamicModelHolder 类。
此功能在 Build #618 后可用，之前的版本不支持此类或存在问题，请更新至最新版本。

源码为 DynamicModelHolder.java 文件。

首先使用 ` new DynamicModelHolder() ` 关键字创建一个新的 DynamicModelHolder 实例

- `DynamicModelHolder.uploadLater(rawModel: RawModel): void ` 

    将 rawModel 添加到上传队列中，稍后（在接下来某一帧时主线程上）会将它上传为 ModelCluster，成为新的 uploadedModel。

- `DynamicModelHolder.uploadNow(rawModel: RawModel): void`

    立即上传一个 RawModel 为 ModelCluster，成为新的 uploadedModel。
    必须在初始化或渲染线程中调用。

- `DynamicModelHolder.getUploadedModel(): ModelCluster | null`

    获取已上传的 ModellCluster。如果未进行过上传操作，或 `uploadLater` 刚刚调用操作还没实际进行，会返回 `null`。


- `DynamicModelHolder.close(): void`

    关闭 DynamicModelHolder 实例，释放资源。同时 `uploadedModel` 也将不再可用。




## 示例代码

### 示例1：加载一个 OBJ 模型为 RawModel，翻转 RawModel 的 UV y坐标，使用 ModelManager 上传 RawModel 得到一个 ModelCluster。

```javascript

//加载一个rawModel
let rawModel = ModelManager.loadRawModel(Resources.manager(), Resources.id("mtr:models/cube.obj"), null);

//翻转 V 坐标

rawModel.applyUVMirror(false, true);

//上传得到一个ModelCluster
let modelCluster = ModelManager.uploadVertArrays(rawModel);

```

### 示例2：使用RawMeshBuilder创建RawModel，并生成法线，最终使用 DynamicModelHolder 上传 RawModel 得到一个 ModelCluster。

```javascript

function create(ctx, state, block) {
    //创建一个RawModel
    let rawModel = new RawModel();

    //创建一个RawMeshBuilder
    let rawModelBuilder = new RawMeshBuilder(4, "interior", Resources.id("minecraft:textures/misc/white.png"));

    //设置顶点
    rawModelBuilder.vertex(0.5, 0.5, 0).normal(0, 0, 0).uv(0, 0).endVertex()
    .vertex(0.5, -0.5, 0).normal(0, 0, 0).uv(0, 1).endVertex()
    .vertex(-0.5, -0.5, 0).normal(0, 0, 0).uv(1, 1).endVertex()
    .vertex(-0.5, 0.5, 0).normal(0, 0, 0).uv(1, 0).endVertex();

    //上传为RawModel
    rawModel.append(rawModelBuilder.getMesh());

    //生成法线
    rawModel.generateNormals();

    //声明一个DynamicModelHolder并存入state
    state.dynamicModelHolder = new DynamicModelHolder();

    //添加到上传队列
    state.dynamicModelHolder.uploadLater(rawModel);
}

function render(ctx, state, block) {
    //得到ModelCluster
    if(state.dynamicModelHolder.getUploadedModel()!= null) {
    state.model = dynamicModelHolder.getUploadedModel();
    }
}

```

### 示例3：几个关于VertAttrState的示例函数。

```javascript

function alterAllRGBA (modelCluster, red ,green , blue, alpha) {
    let vertarray = modelCluster.uploadedTranslucentParts.meshList;
    let vert = vertarray[0];
    for(let i = 0; i < vertarray.length; i++) {
        vert = vertarray[i];
        vert.materialProp.attrState.setColor(red , green , blue , alpha);
    }
    vertarray = modelCluster.uploadedOpaqueParts.meshList;
    vert = vertarray[0];
    for(let i = 0; i < vertarray.length; i++) {
        vert = vertarray[i];
        vert.materialProp.attrState.setColor(red , green , blue , alpha);
    }
}

function getAllColor (modelCluster) {
    let result = [];
    let vertarray = modelCluster.uploadedTranslucentParts.meshList;
    let vert = vertarray[0];
    for(let i = 0; i < vertarray.length; i++) {
        vert = vertarray[i];
        result.push(vert.materialProp.attrState.color);
    }
    vertarray = modelCluster.uploadedOpaqueParts.meshList;
    vert = vertarray[0];
    for(let i = 0; i < vertarray.length; i++) {
        vert = vertarray[i];
        result.push(vert.materialProp.attrState.color);
    }
    return result;
}

function alterAllAlpha(modelCluster, newAlpha) {
    let vertarrays = modelCluster.uploadedTranslucentParts.meshList;
    let vertarray = vertarrays[0];
    let color = vertarray.materialProp.attrState.color;
    let red=0,green=0,blue=0;
    for(let i = 0; i < vertarrays.length; i++) {
        vertarray = vertarrays[i];
        color = vertarray.materialProp.attrState.color;
        red = (color >> 16) & 0xFF;
        green = (color >> 8) & 0xFF;
        blue = color & 0xFF;
        vertarray.materialProp.attrState.setColor(red , green , blue , newAlpha);
    }
    vertarrays = modelCluster.uploadedOpaqueParts.meshList;
    vertarray = vertarrays[0];
    color = vertarray.materialProp.attrState.color;
    red=0,green=0,blue=0;
    for(let i = 0; i < vertarrays.length; i++) {
        vertarray = vertarrays[i];
        color = vertarray.materialProp.attrState.color;
        red = (color >> 16) & 0xFF;
        green = (color >> 8) & 0xFF;
        blue = color & 0xFF;
        vertarray.materialProp.attrState.setColor(red , green , blue , newAlpha);
    }
}

``` 