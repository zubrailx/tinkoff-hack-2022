package com.example.tinkofftradingrobot.strategy.solution.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolutionResponse {
    List<Resolution> resolutions;
    String accountID;
}

