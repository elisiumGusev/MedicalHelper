package com.llsparrow.healthassistant.feature_account_api.domain

import com.llsparrow.healthassistant.feature_account_api.domain.model.User
import io.reactivex.Completable
import io.reactivex.Maybe

interface UserInteractor {

    fun getUser(fromCache: Boolean): Maybe<User>

    fun saveUser(userModel: User): Completable

    fun updateUser(userModel: User): Completable

    fun clear(): Completable
}
