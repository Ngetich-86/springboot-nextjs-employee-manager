package com.pesira.backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pesira.backend.dto.employee.EmployeeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EmployeeExportUtils {

    private static final String CSV_HEADER =
            "id,firstName,lastName,email,department,position,salary,hireDate,createdAt,updatedAt";

    private final ObjectMapper objectMapper;

    public byte[] toCsv(List<EmployeeResponse> employees) {
        StringWriter writer = new StringWriter();
        writer.write(CSV_HEADER);
        writer.write('\n');

        for (EmployeeResponse employee : employees) {
            writer.write(String.join(",",
                    value(employee.getId()),
                    value(employee.getFirstName()),
                    value(employee.getLastName()),
                    value(employee.getEmail()),
                    value(employee.getDepartment()),
                    value(employee.getPosition()),
                    value(employee.getSalary()),
                    value(employee.getHireDate()),
                    value(employee.getCreatedAt()),
                    value(employee.getUpdatedAt())));
            writer.write('\n');
        }

        return writer.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] toJson(List<EmployeeResponse> employees) throws IOException {
        ObjectMapper exportMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return exportMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(employees);
    }

    private String value(Object field) {
        if (field == null) {
            return "";
        }
        String stringValue = field.toString();
        if (stringValue.contains(",") || stringValue.contains("\"") || stringValue.contains("\n")) {
            return "\"" + stringValue.replace("\"", "\"\"") + "\"";
        }
        return stringValue;
    }
}
