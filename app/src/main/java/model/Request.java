package model;

/**
 * Created by DBBL on 1/8/2018.
 */

public interface Request<ResultType> {
    void onResponse(ResultType result, boolean responseStatus);
}
