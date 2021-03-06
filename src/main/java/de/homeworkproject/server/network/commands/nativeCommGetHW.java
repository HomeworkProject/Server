package de.homeworkproject.server.network.commands;

import de.homeworkproject.server.allocation.HWUser;
import de.homeworkproject.server.homework.HomeWork;
import de.homeworkproject.server.network.Error;
import de.homeworkproject.server.network.HWClientCommandContext;
import de.homeworkproject.server.network.Status;
import de.homeworkproject.server.network.Types;
import de.homeworkproject.server.reflections.HWCommandHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Life4YourGames on 28.06.16.
 */

@HWCommandHandler
public class nativeCommGetHW extends nativeCommandParent {

    public static final String IDENTIFIER = "de.mlessmann.commands.gethw";
    public static final String COMMAND = "gethw";

    public nativeCommGetHW() {
        super();
        setID(IDENTIFIER);
        setCommand(COMMAND);
    }

    @Override
    public CommandResult onMessage(HWClientCommandContext context) {
        super.onMessage(context);
        JSONArray subjects = null;
        if (context.getRequest().has("subjects")) {
            subjects = context.getRequest().optJSONArray("subjects");
        }

        if (!requireUser(context.getHandler())) {
            return CommandResult.clientFail();
        }


        Optional<HWUser> u = context.getHandler().getUser();
        //IsPresent checked in #requireUser(HWTCPClientReference) above
        //noinspection OptionalGetWithoutIsPresent
        HWUser myUser = u.get();

        if (!context.getRequest().has("date")) {

            if (!require(context.getRequest(), "dateFrom", context.getHandler())) {
                return CommandResult.clientFail();
            }
            if (!require(context.getRequest(), "dateTo", context.getHandler())) {
                return CommandResult.clientFail();
            }

            try {

                JSONArray fromDate = context.getRequest().getJSONArray("dateFrom");

                JSONArray toDate = context.getRequest().getJSONArray("dateTo");

                int fyyyy = fromDate.getInt(0);
                int fMM = fromDate.getInt(1);
                int fdd = fromDate.getInt(2);

                int tyyyy = toDate.getInt(0);
                int tMM = toDate.getInt(1);
                int tdd = toDate.getInt(2);

                JSONObject p = Status.state_PROCESSING();
                p.put("commID", context.getHandler().getCurrentCommID());
                sendJSON(p);

                LocalDate dateFrom = LocalDate.of(fyyyy, fMM, fdd);
                LocalDate dateTo = LocalDate.of(tyyyy, tMM, tdd);

                //Filter subjects
                ArrayList<String> subjectFilter = null;
                if (subjects != null && subjects.length() > 0) {
                    subjectFilter = new ArrayList<String>();
                    ArrayList<String> finalSubjectFilter = subjectFilter;
                    subjects.forEach(s -> {
                                if (s instanceof String) {
                                    finalSubjectFilter.add((String) s);
                                }
                            }
                    );
                }

                List<HomeWork> hws = myUser.getHWBetween(dateFrom, dateTo, subjectFilter, false);

                JSONObject response = new JSONObject();
                response.put("status", Status.OK);
                response.put("status_message", Status.SOK);
                JSONArray arr = new JSONArray();
                hws.forEach(hw ->
                        {
                            if (hw.read() && hw.isValid()) {
                                arr.put(hw.getJSON());
                            }
                        }
                );
                response.put("payload_type", Types.JSONArray);
                response.put("array_type", Types.HWObject);
                response.put("payload", arr);
                response.put("commID", context.getHandler().getCurrentCommID());
                sendJSON(response);
                return CommandResult.success();

            } catch (JSONException ex) {

                JSONObject response = new JSONObject();

                response.put("status", Status.BADREQUEST);
                response.put("payload_type", "error");

                JSONObject e = new JSONObject();
                e.put("error", Error.BadRequest);
                e.put("error_message", ex.toString());
                e.put("friendly_message", "Client sent an invalid request");
                response.put("payload", e);

                response.put("commID", context.getHandler().getCurrentCommID());

                sendJSON(response);

                return CommandResult.clientFail();

            } catch (DateTimeException ex) {

                JSONObject response = new JSONObject();

                response.put("status", Status.BADREQUEST);
                response.put("payload_type", "error");

                JSONObject e = new JSONObject();
                e.put("error", Error.DateTimeError);
                e.put("error_message", ex.toString());
                e.put("friendly_message", "Client sent an invalid request");
                response.put("payload", e);

                response.put("commID", context.getHandler().getCurrentCommID());

                sendJSON(response);

                return CommandResult.clientFail();

            }

        } else {

            try {

                JSONArray datArr = context.getRequest().getJSONArray("date");

                LocalDate date = LocalDate.of(datArr.getInt(0), datArr.getInt(1), datArr.getInt(2));

                JSONObject p = Status.state_PROCESSING();
                p.put("commID", context.getHandler().getCurrentCommID());
                sendJSON(p);

                //Subject filter
                ArrayList<String> subjectFilter = null;
                if (subjects != null && subjects.length() > 0) {
                    subjectFilter = new ArrayList<String>();
                    ArrayList<String> finalSubjectFilter = subjectFilter;
                    subjects.forEach(s -> {
                        if (s instanceof String) {
                            finalSubjectFilter.add((String) s);
                        }
                    });
                }

                ArrayList<HomeWork> hws = myUser.getHWOn(date, subjectFilter);

                JSONObject response = new JSONObject();
                response.put("status", Status.OK);
                JSONArray arr = new JSONArray();
                hws.forEach(hw -> arr.put(hw.getJSON()));
                response.put("payload_type", "JSONArray");
                response.put("array_type", "HWObject");
                response.put("payload", arr);
                response.put("commID", context.getHandler().getCurrentCommID());
                sendJSON(response);
                return CommandResult.success();

            } catch (JSONException ex) {

                JSONObject response = new JSONObject();
                response.put("status", Status.BADREQUEST);
                response.put("payload_type", "error");
                JSONObject e = new JSONObject();
                e.put("error", Error.BadRequest);
                e.put("error_message", ex.toString());
                e.put("friendly_message", "Client sent an invalid request");
                response.put("payload", e);
                response.put("commID", context.getHandler().getCurrentCommID());
                sendJSON(response);
                return CommandResult.clientFail();

            } catch (DateTimeException ex) {

                JSONObject response = new JSONObject();
                response.put("status", Status.BADREQUEST);
                response.put("payload_type", "error");
                JSONObject e = new JSONObject();
                e.put("error", Error.DateTimeError);
                e.put("error_message", ex.toString());
                e.put("friendly_message", "Client sent an invalid request");
                response.put("payload", e);
                response.put("commID", context.getHandler().getCurrentCommID());
                sendJSON(response);
                return CommandResult.clientFail();
            }
        }
    }
}
