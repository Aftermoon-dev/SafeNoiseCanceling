package kr.ac.gachon.sw.safenoisecanceling.utils

import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.*

class RxEventBus {
    companion object {
        private val reActiveXTable: Hashtable<String, PublishSubject<Any>> = Hashtable<String, PublishSubject<Any>>()
        private var instance: RxEventBus? = null

        @Synchronized
        fun getInstance(): RxEventBus? {
            if(instance == null) {
                synchronized(RxEventBus::class) {
                    instance = RxEventBus()
                }
            }
            return instance
        }
    }
    /**
     * Subscribe한 모든 곳에 Event 전송
     * @param key Event Unique Key
     * @param data Data
     * @author Minjae Seon
     */
    fun sendEvent(key: String, data: Any) {
        reActiveXTable[key]?.onNext(data)
    }

    /**
     * Sender가 보낸 Event 받기
     * @param key Event Unique Key
     * @return 각 Key에 해당하는 Subject
     * @author Minjae Seon
     */
    fun receiveEvent(key: String): PublishSubject<Any> {
        synchronized(this) {
            if(!reActiveXTable.containsKey(key)) {
                reActiveXTable[key] = PublishSubject.create()
            }
            return reActiveXTable[key]!!
        }
    }
}