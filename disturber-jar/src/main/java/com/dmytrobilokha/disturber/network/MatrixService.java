package com.dmytrobilokha.disturber.network;

import com.dmytrobilokha.disturber.network.dto.EventContentDto;
import com.dmytrobilokha.disturber.network.dto.LoginAnswerDto;
import com.dmytrobilokha.disturber.network.dto.LoginPasswordDto;
import com.dmytrobilokha.disturber.network.dto.SendEventResponseDto;
import com.dmytrobilokha.disturber.network.dto.SyncResponseDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * The interface represents matrix server
 */
interface MatrixService {

    @GET("_matrix/client/r0/sync")
    Call<SyncResponseDto> sync(@Query("access_token") String accessToken);

    @GET("_matrix/client/r0/sync")
    Call<SyncResponseDto> sync(
            @Query("access_token") String accessToken
            , @Query("since") String since
            , @Query("timeout") int timeout);

    @POST("_matrix/client/r0/login")
    Call<LoginAnswerDto> login(@Body LoginPasswordDto loginPassword);

    @PUT("_matrix/client/r0/rooms/{roomId}/send/{eventType}/{txnId}")
    Call<SendEventResponseDto> sendMessageEvent(
            @Path("roomId") String roomId
            , @Path("eventType") String eventType
            , @Path("txnId") String txnId
            , @Query("access_token") String accessToken
            , @Body EventContentDto eventContentDto);
}
