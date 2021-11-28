package kr.ac.gachon.sw.safenoisecanceling.models

import kr.ac.gachon.sw.safenoisecanceling.ApplicationClass

object DatabaseModel {
    /**
     * 새로운 분류 데이터를 DB에 저장
     * @author Minjae Seon
     * @param soundType Sound Type
     * @param score Classify Score
     */
    fun writeNewClassificationData(soundType: String, score: Float) {
        // 인식된 정보를 DB에 저장
        ApplicationClass.roomDatabase.soundDao().insert(Sound(soundType = soundType, score = score, time = System.currentTimeMillis()))
    }

    /**
     * Index를 이용해 Sound Data 가져오기
     * @author Minjae Seon
     * @param startIdx 시작 Index
     * @param itemCnt 가져올 Item 갯수
     * @return List<Sound>
     */
    fun getSoundDatabyIndex(startIdx: Int, itemCnt: Int): List<Sound> {
        return ApplicationClass.roomDatabase.soundDao().getSoundDatabyIndex(startIdx, itemCnt)
    }
}