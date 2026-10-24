package dev.challenge.conduit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

@Controller("/api/tags")
@ExecuteOn(TaskExecutors.BLOCKING)
public class TagsController {

    private final DataSource dataSource;

    public TagsController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Get
    public Map<String, List<String>> list() throws SQLException {
        var tags = new ArrayList<String>();

        try (var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(
                "SELECT name FROM tags ORDER BY name"
            );
            var result = statement.executeQuery()) {

            while (result.next()) {
                tags.add(result.getString(1));
            }
        }

        return Map.of("tags", tags);
    }
}