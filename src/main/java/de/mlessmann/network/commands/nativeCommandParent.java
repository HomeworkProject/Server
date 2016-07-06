package de.mlessmann.network.commands;

import de.mlessmann.network.HWClientCommandContext;
import de.mlessmann.network.HWTCPClientReference;
import de.mlessmann.network.Status;
import de.mlessmann.util.Common;
import org.json.JSONObject;
import de.mlessmann.network.Error;

import java.util.Optional;

/**
 * Created by Life4YourGames on 29.06.16.
 */
public abstract class nativeCommandParent implements ICommandHandler {

    private String ID = "de.mlessmann.command.native";
    private String COMM = "";
    private boolean critical = false;

    protected void setID(String id) { ID = id; }

    protected void setCommand(String command) { COMM = command; }

    protected void setCritical(boolean c) { critical = c; }

    public String getIdentifier() { return ID; }

    public String getCommand() { return COMM; }

    public boolean isCritical() { return critical; }

    public abstract boolean onMessage(HWClientCommandContext context);

    @Override
    public Optional<ICommandHandler> clone() {

        ICommandHandler res = null;

        try {

            res = this.getClass().newInstance();

        } catch (Exception e) {

        }

        return Optional.ofNullable(res);
    }

    //BEGIN UTIL METHODS

    protected boolean require(JSONObject request, String field, HWTCPClientReference r) {

        if (request.has(field)) {
            return true;
        }

        JSONObject response = new JSONObject();
        response.put("status", Status.BADREQUEST);
        response.put("payload_type", "error");

        JSONObject e = new JSONObject();

        e.put("error", Error.BadRequest);
        e.put("error_message", "Request is missing field \"" + field + "\"!");
        e.put("friendly_message", "Request was incomplete, contact your client developer");
        response.put("payload", e);

        response.put("commID", Common.negateInt(r.getCurrentCommID()));

        r.sendJSON(response);

        return false;

    }

    protected boolean requireUser(HWTCPClientReference r) {

        if (!r.getUser().isPresent()) {

            JSONObject response = Status.state_ERROR(
                    Status.UNAUTHORIZED,
                    Status.state_genError(
                            Error.LoginRequired,
                            "Login request was missing or failed previously",
                            "Please log in first"
                    )
            );

            response.put("commID", r.getCurrentCommID());

            sendJSON(r, response);

            return false;
        }

        return true;

    }

    protected void sendJSON(HWTCPClientReference r, JSONObject json) {

        json.put("handler", this.getIdentifier());

        r.sendJSON(json);

    }

}