# 高阶技巧

## 异步编程

在 (A)NTE 中，会间隔调用 `render` 方法，但存在诸多问题，如内容过多导致的延迟过大进而导致动画不流畅等诸多问题。不难发现，许多任务是可以同时执行的。因此，我们可以使用 `java` 提供的异步编程机制，为每个 `entity` 创建一个自己的线程。

下面是一段示例代码:

```javascript
importPackage(java.lang);
importPackage(java.util.concurrent);

function create(ctx, state, entity) {
    state.running = true;
    state.lastTime = Date.now();
    state.thread = new Thread(() => {
        // 创建一个线程池，具体线程池可根据需求量调整
        let pool = Executors.newScheduledThreadPool(1);
        
        // 添加定时任务，每秒24次
        pool.scheduleAtFixedRate(new Runnable({run: () => {
            // 任务代码，例如：
            state.lastTime = Date.now();

            // ······

            // 在最上方添加调试信息
            ctx.setDebugInfo("lastTime", PlacementOrder.UPSIDE, state.lastTime);

            // 更新绘制，在这里不要使用drawModel、playSound等方法
            // 这些方法仅在render中使用时正确的，在异步线程中调用会导致线程安全问题
            ctx.drawCalls.put(123, new ClusterDrawCall(model, new Matrix4f()));
        }}, 0, 1000 / 24, TimeUnit.MILLISECONDS));
        
        while (state.running) {
            // 也可以用于处理事件，但频率不一定固定
            Thread.sleep(1000);
        }

        // 关闭线程池
        pool.shutdown();
    })
    state.thread.start();
}

function dispose(ctx, state, entity) {
    state.running = false;
}

```

实际应用时，**请一定注意线程安全!!!**