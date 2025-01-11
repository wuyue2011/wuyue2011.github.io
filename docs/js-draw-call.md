# JavaScript DrawCall

在 ANTE 中，使用 `DrawCall` 类实现绘制调用实现。

`DrawCall` 是一个接口, 它仅含有一个方法:

- `void commit(drawScheduler: DrawScheduler, basePose: Matrix4f, worldPose: Matrix4f, light: int)`
该方法的作用是提交绘制调用。

## 自定义实现

ANTE 允许您自己实现 `DrawCall` 接口，可以使用自己的变化逻辑。
下面是一个示例：

```javascript
    let model, pose;
    let drawCall = new DrawCall({commit: (drawScheduler, basePose, worldPose, light) => {
        let finalPose = worldPose.copy();
        finalPose.multiply(pose);
        // ···
        drawScheduler.enqueue(model, finalPose, light);
    }})
```
在这段示例中，我们制作了一个实现了 `DrawCall` 接口的对象，它可以在需要 `DrawCall` 的地方当做 `DrawCall` 使用。
每次渲染都会调用 `commit` 方法，可以在 `commit` 里放入自己的变化逻辑。

## ClusterDrawCall

`ClusterDrawCall` 是 `DrawCall` 的一个实现，使用 `basePose`。
它有以下两种构造函数：

- `new ClusterDrawCall(model: ModelCluster, pose: Matrix4f)`
- `new ClusterDrawCall(model: DynamicModelHolder, pose: Matrix4f)`

## WorldDrawCall

`WorldDrawCall` 是 `DrawCall` 的一个实现，使用 `worldPose`。
它有以下两种构造函数：

- `new WorldDrawCall(model: Model, pose: Matrix4f)`
- `new WorldDrawCall(model: DynamicModel, pose: Matrix4f)`