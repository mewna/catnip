package com.mewna.catnip.rest.guild;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.mewna.catnip.util.JsonConvertible;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

@Accessors(fluent = true)
@Getter
@Setter
public class MemberAddOptions implements JsonConvertible {

    private String accessToken;
    private String nickname;
    private List<String> roles;
    private Boolean mute;
    private Boolean deaf;

    @Nonnull
    @CheckReturnValue
    public static MemberAddOptions create() {
        return new MemberAddOptions();
    }
    
    @Nonnull
    @Override
    public JsonObject toJson() {
        final JsonObject object = new JsonObject();
        object.put("access_token", accessToken);
        if(roles != null) {
            final JsonArray array = new JsonArray();
            array.addAll(roles);
            object.put("roles", array);
        }
        if(nickname != null) {
            object.put("nick", nickname);
        }
        if(mute != null) {
            object.put("mute", mute);
        }
        if(deaf != null) {
            object.put("deaf", deaf);
        }
        return object;
    }
}
