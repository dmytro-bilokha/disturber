package com.dmytrobilokha.disturber.service.network;

import com.dmytrobilokha.disturber.model.network.MessageDto;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by dimon on 13.08.17.
 */
interface MatrixService {

    @GET("random")
    Call<MessageDto> getQuote();

}
