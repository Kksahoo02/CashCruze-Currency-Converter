package api;

import model.ConversionResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CurrencyService {
    @GET("latest")
    Call<ConversionResponse> convertCurrency(
            @Query("from") String fromCurrency,
            @Query("to") String toCurrency,
            @Query("amount") double amount
    );
}