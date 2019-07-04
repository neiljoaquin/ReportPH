package com.neil.reportph.firebase

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.neil.reportph.Logger
import com.neil.reportph.models.Reports
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.SingleEmitter


class FirebaseStorage {
    private val TAG = "FirebaseStorage"
    private val LONGTITUDE = "longtitude"
    private val LATITUDE = "latitude"
    private val REPORTS = "reports"
    private var singleEmitter: SingleEmitter<Boolean>? = null
    private var observableEmitter: ObservableEmitter<Reports>? = null

    fun getSingleObservable(): Single<Boolean> {
        return Single.create<Boolean>{emitter -> this.singleEmitter = emitter }
    }

    fun getObservable(): Observable<Reports> {
        return Observable.create<Reports>{emitter -> this.observableEmitter = emitter}
    }

    fun addReportToFireStore(report: Reports) {
        // Access a Cloud Firestore instance from your Activity
        val db = FirebaseFirestore.getInstance()
        // Add a new document with a generated ID
        db.collection(REPORTS)
            .add(report)
            .addOnSuccessListener { documentReference ->
                Logger.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                singleEmitter?.onSuccess(true)
            }
            .addOnFailureListener { e ->
                Logger.w(TAG, "Error adding document", e)
                singleEmitter?.onSuccess(false)
            }
    }

    fun searchLatLngRange(latMin: Double, latMax: Double, longMin: Double, longMax: Double) {
        val db = FirebaseFirestore.getInstance()
        val ref = db.collection("reports")
        val longtitudeRange = ref.whereLessThan(LONGTITUDE, longMax)
            .whereGreaterThan(LONGTITUDE, longMin)
        val latitudeRange = ref.whereLessThan(LATITUDE, latMax)
            .whereGreaterThan(LATITUDE, latMin)
        val longTask = longtitudeRange.get()
        val latTask = latitudeRange.get()

        Tasks.whenAllSuccess<Any>(longTask, latTask).addOnSuccessListener {
                lists -> findIntersectionInQuery(lists)
        }
    }

    private fun findIntersectionInQuery(list: List<Any>) {
        for (item in list) {
            val documents = item as QuerySnapshot

            Logger.d(TAG, "Are queries empty: ${documents.isEmpty}")
            if (documents.isEmpty) {
                Logger.d(TAG, "No intersection in database")
                observableEmitter?.onComplete()
                return
            }
            for (document in documents) {
                val report = document.toObject(Reports::class.java)
                Logger.d(TAG, "$report")
                observableEmitter?.onNext(report)
            }
            observableEmitter?.onComplete()
        }
    }

}

