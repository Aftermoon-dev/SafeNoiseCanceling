package kr.ac.gachon.sw.safenoisecanceling.models

import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass

object DatabaseModel {
    fun writeNewClassificationData(soundType: String, score: Float) {
        // 인식된 정보를 DB에 저장
        ApplicationClass.roomDatabase.soundDao().insert(Sound(soundType = soundType, score = score, time = System.currentTimeMillis()))
    }
}