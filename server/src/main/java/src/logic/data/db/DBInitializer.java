package src.logic.data.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInitializer {

    private static final String initalizeScript = """
            create table if not exists Coordinates (
                                         id serial primary key,
                                         x bigint not null check ( x <= 955 ),
                                         y integer not null check ( y <= 207 )
            );
                        
            DO $$
                BEGIN
                    IF NOT EXISTS (SELECT FROM pg_tables
                                   WHERE schemaname = 'public' AND tablename  = 'color') THEN
                        create table if not exists Color (
                            id serial primary key,
                            color varchar(20) not null
                        );
                        
                        insert into color (color)
                        values ('RED'),
                               ('YELLOW'),
                               ('BROWN');
                    END IF;
                END $$;
                        
            create table if not exists Person (
                                    id serial primary key,
                                    name varchar not null check ( name not like '' ),
                                    birthday timestamp,
                                    height numeric(1000, 3) not null check ( height > 0 ),
                                    passportID varchar(38) not null check ( length(passportID) >= 4 ),
                                    heirColor integer references Color on delete set null
            );
                        
            create table if not exists Dragon (
                                    id serial primary key,
                                    name varchar not null check ( name not like '' ),
                                    coordinates integer references Coordinates on delete set default default 0 not null,
                                    creationDate timestamp not null default localtimestamp,
                                    age bigint check ( age > 0),
                                    wingspan integer check ( wingspan > 0),
                                    weight numeric(1000, 3) check ( weight > 0) not null,
                                    color integer references Color on delete set default default 0 not null,
                                    killer integer references Person on delete set null
            );
                        
            create table if not exists users (
                                   id serial primary key,
                                   login varchar(50) not null check ( login not like '' ) unique ,
                                   password bytea not null,
                                   salt bytea not null
            );
                        
            create table if not exists users_dragon (
                                          users integer references users not null,
                                          dragon integer references dragon on delete cascade,
                                          primary key (users, dragon)
            );
            """;

    public static void configureDB(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(initalizeScript);
    }
}
