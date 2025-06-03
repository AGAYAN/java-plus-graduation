package ru.practicum.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.action.UserActionControllerGrpc;
import ru.practicum.grpc.stats.action.UserActionMessage;
import ru.practicum.service.CollectorService;

import java.time.Instant;

@GrpcService
@RequiredArgsConstructor
public class CollectorController extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final CollectorService collectorService;

    @Override
    public void collectUserAction(UserActionMessage.UserActionRequest request,
                                  StreamObserver<UserActionMessage.UserActionResponse> responseObserver) {
        ActionTypeAvro actionTypeAvro = switch (request.getActionType()) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> null;
        };

        collectorService.sendAction(new UserActionAvro(
                request.getUserId(),
                request.getEventId(),
                actionTypeAvro,
                Instant.ofEpochSecond(
                        request.getTimestamp().getSeconds(),
                        request.getTimestamp().getNanos()
                )
        ));

        UserActionMessage.UserActionResponse response = UserActionMessage.UserActionResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
