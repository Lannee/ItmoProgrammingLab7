package src;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import module.connection.responseModule.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import module.connection.IConnection;
import module.connection.requestModule.Request;
import module.logic.streams.ConsoleInputManager;
import module.logic.streams.ConsoleOutputManager;
import module.logic.streams.InputManager;
import module.logic.streams.OutputManager;
import src.authorization.Authorization;
import src.commands.Invoker;
import src.connection.DatagramConnection;
import src.logic.data.Receiver;
import src.logic.data.db.DBConfParser;

public class Server {
    private final static int SERVER_PORT = 50689;
    private boolean running = true;

    public final static String invite = ">>>";

    public static final InputManager in = new ConsoleInputManager();
    public static final OutputManager out = new ConsoleOutputManager();

    private IConnection connection;
    private Connection dbConnection;

    private Invoker invoker;
    private Authorization authorization;
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private ExecutorService executorServiceForReceivingRequests = Executors.newFixedThreadPool(5);
    private ExecutorService executorServiceForExecutingCommands = Executors.newCachedThreadPool();

    public void start() {

        logger.info("Starting server.");

        try {
            DBConfParser conf = new DBConfParser();

            connection = new DatagramConnection(SERVER_PORT, true);
            authorization = new Authorization(conf);

            invoker = new Invoker(connection, authorization,
                    new Receiver(conf));

            logger.info("Invoker and Receiver started.");
        } catch (SocketException e) {
            logger.error("This address is currently in use.");
            running = false;
        } catch (UnknownHostException e) {
            running = false;
            logger.error("Unknown host.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
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
            Callable<Request> callableGetRequest = () -> {
                return (Request) connection.packetConsumer();
            };
            Request request;
            try {
                request = executorServiceForReceivingRequests.submit(callableGetRequest).get();
                InetAddress clientHost = connection.getRecipientHost();
                int clientPort = connection.getRecipientPort();

                logger.info("Received request from client with command '{}' and arguments '{}'",
                        request.getCommandName(), request.getArgumentsToCommand());

                switch (request.getTypeOfRequest()) {
                    case COMMAND, CONFIRMATION -> {
                        executorServiceForExecutingCommands.submit(() -> {
                            String result = invoker.parseRequest(request);
                            if (result.equals("WAITING")) {
                                connection.send(clientHost, clientPort,
                                        new CommandResponse(result, ResponseStatus.WAITING));
                            } else {
                                connection.send(clientHost, clientPort, new CommandResponse(result));
                                logger.info("Response sent.");
                            }
                        });
                    }
                    case INITIALIZATION -> {
                        Response response;
                        response = new CommandsDescriptionResponse(invoker.getAuthenticatedCommandsDescriptions(),
                                invoker.getNonAuthenticatedCommandsDescription());
//                        if (request.getUserName() == null) {
//                            response = new CommandsDescriptionResponse(invoker.getNonAuthenticatedCommandsDescription());
//                        } else {
//                            response = new CommandsDescriptionResponse(invoker.getAuthenticatedCommandsDescriptions());
//                        }
                        connection.send(clientHost, clientPort, response);
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
