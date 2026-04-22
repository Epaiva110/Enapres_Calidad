package com.minedu.gob.pe.encuestasatisfaccinenapres.sesion

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IdleSessionManager(
    private val timeoutMillis: Long = 1 * 10 * 1000,
    private val onTimeout: () -> Unit
) {

    private var job: Job? = null

    fun reset(scope: CoroutineScope) {

        job?.cancel()

        job = scope.launch {

            delay(timeoutMillis)
            onTimeout()

        }

    }

}