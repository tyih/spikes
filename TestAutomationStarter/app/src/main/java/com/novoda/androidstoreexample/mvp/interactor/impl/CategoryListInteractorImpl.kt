package com.novoda.androidstoreexample.mvp.interactor.impl

import android.content.Context
import com.novoda.androidstoreexample.models.CategoryResponse
import com.novoda.androidstoreexample.mvp.interactor.CategoryListInteractor
import com.novoda.androidstoreexample.mvp.listener.CategoryListListener
import com.novoda.androidstoreexample.network.ShopService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

class CategoryListInteractorImpl: CategoryListInteractor {
    val retrofit: Retrofit
    val apiService: ShopService
    val appContext: Context

    lateinit var call: Call<CategoryResponse>

    @Inject
    constructor(retrofit: Retrofit, apiService: ShopService, appContext: Context) {
        this.retrofit = retrofit
        this.apiService = apiService
        this.appContext = appContext
    }


    override fun loadCategoryList(categoryListListener: CategoryListListener) {
        call = apiService.getCategories()
        call.enqueue(object : Callback<CategoryResponse>{
            override fun onResponse(call: Call<CategoryResponse>?, response: Response<CategoryResponse>?) {
                if (response != null && response.isSuccessful) {
                    categoryListListener.onSuccess(response.body()!!)
                } else {
                    categoryListListener.onFailure("Error while fetching data")
                }
            }

            override fun onFailure(call: Call<CategoryResponse>?, t: Throwable?) {
                categoryListListener.onFailure("Error while fetching data")
            }
        })

    }

    override fun cancel() {
        call.cancel()
    }
}