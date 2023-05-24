package src;

import module.connection.IConnection;
import module.connection.requestModule.Request;
import module.connection.responseModule.*;
import module.logic.streams.ConsoleInputManager;
import module.logic.streams.ConsoleOutputManager;
import module.logic.streams.InputManager;
import module.logic.streams.OutputManager;
import src.commands.Invoker;
import src.connection.DatagramConnection;
import src.logic.data.Receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final static int SERVER_PORT = 50689;
    private boolean running = true;

    public final static String invite = ">>>";

    public static final InputManager in = new ConsoleInputManager();
    public static final OutputManager out = new ConsoleOutputManager();

    private IConnection connection;

    private Invoker invoker;
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private ExecutorService executorServiceForReceivingRequests = Executors.newFixedThreadPool(5);
    private ExecutorService executorServiceForExecutingCommands = Executors.newCachedThreadPool();

    public void start(String[] args) {

        logger.info("Starting server.");
        String filePath = getFilePath(args);
        // String filePath = getFilePath(new String[]{"FileJ"});

        try {
            connection = new DatagramConnection(SERVER_PORT, true);
            invoker = new Invoker(connection,
                    new Receiver(filePath));
            logger.info("Invoker and Receiver started.");
        } catch (SocketException e) {
            logger.error("This address is currently in use.");
            running = false;
        } catch (UnknownHostException e) {
            running = false;
            logger.error("Unknown host.");
        }

        new Thread(() -> {
            String line;
            while (running) {
                try {
                    if (in.isBufferEmpty()) {
                        if (invoker.getRecursionSize() != 0)
                            invoker.clearRecursion();
                        out.print(invite + " ");
                    }
                    line = in.readLine();
                    invoker.parseCommand(line);
                } catch (IllegalArgumentException iae) {
                    out.print(iae.getMessage() + "\n");
                }
            }
        }).start();

        while (running) {
            byte[] byteArray = connection.receive();
            Callable<Request> callableGetRequest = () -> {
                return (Request) connection.handleByteArray(byteArray);
            };
            Request request;
            try {
                request = executorServiceForReceivingRequests.submit(callableGetRequest).get();

                // Request request = (Request) connection.receive();
                // executorServiceForReceiving.submit(() -> {});
                logger.info("Received request from client with command '{}' and arguments '{}'",
                        request.getCommandName(), request.getArgumentsToCommand());

                switch (request.getTypeOfRequest()) {
                    case COMMAND, CONFIRMATION -> {
                        executorServiceForExecutingCommands.submit(() -> {
                            String result = invoker.parseRequest(request);
                            if (result == "WAITING") {
                                connection.send(new CommandResponse(result, ResponseStatus.WAITING));
                            } else {
                                connection.send(new CommandResponse(result));
                                logger.info("Response sent.");
                            }
                        });
                    }
                    case INITIALIZATION -> {
                        Response response = new CommandsDescriptionResponse(invoker.getCommandsDescriptions());
                        connection.send(response);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getFilePath(String[] args) {
        // return "base.csv";

        if (args.length == 0) {
            logger.error("Incorrect number of arguments.");
            System.exit(2);
        }
        String filePath = System.getenv().get(args[0]);
        if (filePath == null) {
            logger.error("Environment variable \"" + args[0] + "\" does not exist.");
            System.exit(1);
        }
        return filePath;
    }
}
