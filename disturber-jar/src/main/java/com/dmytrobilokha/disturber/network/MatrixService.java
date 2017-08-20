package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.model.network.MessageDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by dimon on 13.08.17.
 */
interface MatrixService {

    @GET("random")
    Call<MessageDto> getQuote();

    @GET("_matrix/client/r0/sync")
    Call<SyncResponseDto> sync();

}
