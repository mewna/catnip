package com.mewna.catnip.rest.handler;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.mewna.catnip.entity.interaction.command.ApplicationCommand;
import com.mewna.catnip.entity.interaction.command.ApplicationCommandOption;
import com.mewna.catnip.entity.interaction.command.ApplicationCommandType;
import com.mewna.catnip.internal.CatnipImpl;
import com.mewna.catnip.rest.ResponsePayload;
import com.mewna.catnip.rest.Routes;
import com.mewna.catnip.rest.requester.Requester;
import com.mewna.catnip.util.JsonUtil;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public class GlobalApplicationCommands extends RestInteraction{
    
    public GlobalApplicationCommands(final CatnipImpl catnip) {
        super(catnip);
    }
    
    public Observable<ApplicationCommand> getGlobalApplicationCommands() {
        return getGlobalApplicationCommandsRaw()
                .map(e -> JsonUtil.mapObjectContents(entityBuilder()::createApplicationCommand).apply(e))
                .flatMapIterable(e -> e);
    }
    public Single<ApplicationCommand> createGlobalApplicationCommand(@Nonnull final ApplicationCommandType type,
                                                                     @Nonnull final String name, @Nullable final String description,
                                                                     @Nonnull final Collection<ApplicationCommandOption> options) {
        return Single.fromObservable(createGlobalApplicationCommandRaw(type, name, description, options)
                .map(entityBuilder()::createApplicationCommand));
    }
    
    public Observable<JsonObject> createGlobalApplicationCommandRaw(@Nonnull final ApplicationCommandType type,
                                                                    @Nonnull final String name, @Nullable final String description,
                                                                    @Nonnull final Collection<ApplicationCommandOption> options) {
        final JsonObject body = createCommandBody(type, name, description, options);
        return catnip().requester().queue(new Requester.OutboundRequest(Routes.CREATE_GLOBAL_APPLICATION_COMMAND
                .withMajorParam(catnip().clientId()), Map.of()).object(body)).map(ResponsePayload::object);
    }
    
    public Single<ApplicationCommand> editGlobalApplicationCommand(@Nonnull final ApplicationCommandType type,
                                                                   @Nonnull final String name, @Nullable final String description,
                                                                   @Nonnull final String commandId,
                                                                   @Nonnull final Collection<ApplicationCommandOption> options) {
        return Single.fromObservable(editGlobalApplicationCommandRaw(type, name, description, commandId, options)
                .map(entityBuilder()::createApplicationCommand));
    }
}
