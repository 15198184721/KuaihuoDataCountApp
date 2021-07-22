package com.kuaihuo.data.count.ext

import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import cn.nekocode.rxlifecycle.LifecycleEvent
import cn.nekocode.rxlifecycle.RxLifecycle
import com.kuaihuo.data.count.KuaihuoCountManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


/**
 *  绑定生命周期和切换线程
 * @receiver Observable<T>
 * @return Observable<T>
 */
fun <T> Observable<T>.ioToMainLifecycle(lifecy: Activity): Observable<T> {
    return this.compose<T>(
        RxLifecycle.bind(lifecy)
            .disposeObservableWhen<T>(LifecycleEvent.DESTROY)
    )
        .ioToMain()
}

/**
 *  绑定生命周期和切换线程
 * @receiver Observable<T>
 * @return Observable<T>
 */
fun <T> Observable<T>.ioToMainLifecycle(lifecy: Fragment): Observable<T> {
    return this.compose<T>(
        RxLifecycle.bind(lifecy)
            .disposeObservableWhen<T>(LifecycleEvent.DESTROY)
    )
        .ioToMain()
}

/**
 *  绑定生命周期和切换线程
 * @receiver Observable<T>
 * @return Observable<T>
 */
fun <T> Observable<T>.ioToMainLifecycle(lifecy: FragmentManager): Observable<T> {
    return this.compose<T>(
        RxLifecycle.bind(lifecy)
            .disposeObservableWhen<T>(LifecycleEvent.DESTROY)
    )
        .ioToMain()
}

/**
 * io切换到主线程
 * @receiver Observable<T>
 * @return Observable<T>
 */
fun <T> Observable<T>.ioToMain(): Observable<T> {
    return this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
}

/**
 * 将任务切换到io线程执行。订阅和发送都在io线程
 * @receiver Observable<T>
 * @return Observable<T>
 */
fun <T> Observable<T>.mainToIo(): Observable<T> {
    return this.subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
}

/**
 * 发起请求,就是执行这个动作。订阅发起执行(所有操作都在子线程中完成)
 * @receiver Observable<T>
 * @param err Function1<Throwable, Unit>
 * @param succ Function1<T, Unit>
 * @return Observable<T>
 */
fun <T> Observable<T>.requestMainToIo(
    err: (Throwable) -> Unit = {},
    succ: (T) -> Unit = {}
): Disposable {
    return this
        .mainToIo()
        .subscribe({
            try {
                succ.invoke(it)
            } catch (e: Exception) {
                KuaihuoCountManager.print("处理网络数据错误了:$e")
            }
        }, {
            err.invoke(it)
            KuaihuoCountManager.print("网络请求发生错误:$it")
        })
}

/**
 * 发起请求,就是执行这个动作。订阅发起执行(发起在子线程，结果在主线程)
 * @receiver Observable<T>
 * @param err Function1<Throwable, Unit>
 * @param succ Function1<T, Unit>
 * @return Observable<T>
 */
fun <T> Observable<T>.requestIoToMain(
    err: (Throwable) -> Unit = {},
    succ: (T) -> Unit = {}
): Disposable {
    return this
        .ioToMain()
        .subscribe({
            try {
                succ.invoke(it)
            } catch (e: Exception) {
                KuaihuoCountManager.print("处理网络数据错误了:$e")
            }
        }, {
            err.invoke(it)
            KuaihuoCountManager.print("网络请求发生错误:$it")
        })
}

