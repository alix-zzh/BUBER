package by.zhuk.buber.receiver;

import by.zhuk.buber.exeption.ReceiverException;
import by.zhuk.buber.model.Coordinate;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Formatter;
import java.util.Optional;

public class GeoDecoder {
    private static final String GEO_DECODER_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=AIzaSyDCd_w_dcctv7LlPuHYIn2dbpA74JSyaVY";
    private static final String RESULTS = "results";
    private static final String GEOMETRY = "geometry";
    private static final String LOCATION = "location";
    private static final String LAT = "lat";
    private static final String LNG = "lng";

    public Optional<Coordinate> decodeCoordinate(String address) throws ReceiverException {
        HttpClient httpClient = new HttpClient();
        address = address.replaceAll(" ", "+");
        StringBuilder stringBuilder = new StringBuilder();
        Formatter formatter = new Formatter(stringBuilder);

        try {
            formatter.format(GEO_DECODER_URL, URLEncoder.encode(address, "UTF-8"));

            GetMethod getMethod = new GetMethod(stringBuilder.toString());

            httpClient.executeMethod(getMethod);
            JSONObject jsonObject = parseResult(getMethod);
            JSONArray jsonArray = (JSONArray) jsonObject.get(RESULTS);
            Optional<Coordinate> optionalCoordinate = Optional.empty();
            if (jsonArray.length() == 0) {
                return optionalCoordinate;
            }
            JSONObject resultJSON = (JSONObject) jsonArray.get(0);
            JSONObject geometryJSON = (JSONObject) resultJSON.get(GEOMETRY);
            JSONObject locationJSON = (JSONObject) geometryJSON.get(LOCATION);

            Coordinate coordinate = new Coordinate();
            coordinate.setLat((float) locationJSON.getDouble(LAT));
            coordinate.setLng((float) locationJSON.getDouble(LNG));

            optionalCoordinate = Optional.of(coordinate);
            return optionalCoordinate;
        } catch (IOException e) {
            throw new ReceiverException(e);
        }
    }

    private JSONObject parseResult(HttpMethod method) throws IOException {
        return new JSONObject(
                new JSONTokener(new InputStreamReader(method.getResponseBodyAsStream())));
    }
}