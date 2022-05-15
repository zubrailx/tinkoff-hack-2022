package com.example.tinkofftradingrobot.strategy.solution.maker;

import com.example.tinkofftradingrobot.config.AlgorithmConfigKeeper;
import com.example.tinkofftradingrobot.strategy.solution.data.SolutionRequest;
import com.example.tinkofftradingrobot.strategy.solution.data.SolutionResponse;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.models.Position;

import java.util.List;
import java.util.Objects;

public class StubSolutionMaker implements SolutionMaker {

    @Override
    public SolutionResponse resolve(InvestApi channel, SolutionRequest solutionData) {
        if (channel.isSandboxMode()) {
            return resolveSandbox(channel, solutionData);
        } else {
            return resolvePublic(channel, solutionData);
        }
    }


    public void resolveOpenedPosition(String figi) {

    }

    /**
     * Возможность купить (необходимо соблюдение всех условий):
     * 1) текущая_цена * (кол-во_в_портфеле + 1) < (капитал * макс_базисных_пунктов_на_инструмент)/10000
     * 2) текущая_цена * 1 < свободные_деньги_в_соответствующей_валюте
     */

    public SolutionResponse resolveSandbox(InvestApi channel, SolutionRequest solutionRequest) {
        SolutionResponse response = new SolutionResponse();
        response.setFigi(solutionRequest.getFigi());
        response.setAccountID(solutionRequest.getAccountID());

        if (Math.random() > 0.9) {
            var portfolio = channel.getOperationsService().getPortfolioSync(solutionRequest.getAccountID());
            var positionList = portfolio.getPositions();
            for (Position position : positionList) {
                if (solutionRequest.getFigi().equals(position.getFigi())) { // позиция уже куплена и ее следует продать
                    response.setOrderDirection(OrderDirection.ORDER_DIRECTION_SELL);
                    response.setQuantity(1);
                    return response;
                }
            }

            String figi = solutionRequest.getFigi();
            var currentPrice = channel.getMarketDataService().getLastPricesSync(List.of(figi)).get(0).getPrice().getUnits()*100 +
                    channel.getMarketDataService().getLastPricesSync(List.of(figi)).get(0).getPrice().getNano();
            var amount = 0;
            for (var pos : positionList) {
                if (pos.getFigi().equals(figi)) {
                    amount = pos.getQuantity().intValue();
                }
            }
            var capital = portfolio.getTotalAmountBonds().getValue().intValue() + portfolio.getTotalAmountEtfs().getValue().intValue() +
                    portfolio.getTotalAmountCurrencies().getValue().intValue() + portfolio.getTotalAmountShares().getValue().intValue() +
                    portfolio.getTotalAmountFutures().getValue().intValue();
            var rubles = 0;
            if (Objects.equals(portfolio.getTotalAmountCurrencies().getCurrency().getCurrencyCode(), "RUB")) {
                rubles = portfolio.getTotalAmountCurrencies().getValue().intValue();
            }

            if (currentPrice * (amount + 1) < (long) capital * AlgorithmConfigKeeper.maxPortfolioBasisPointPerInstrument()
                    && capital > 0 && currentPrice < rubles) {
                // тогда даем распоряжение на покупку
                response.setOrderDirection(OrderDirection.ORDER_DIRECTION_BUY);
                response.setQuantity(1);
                response.setOrderID(generateOrderID());
            }


        }

        return response;
    }

    public SolutionResponse resolvePublic(InvestApi channel, SolutionRequest solutionRequest) {
        return null;
    }

    private String generateOrderID() {
        return "";
    }
}
