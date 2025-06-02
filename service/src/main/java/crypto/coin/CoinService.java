package crypto.coin;

import crypto.coin.exception.CoinNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@RequiredArgsConstructor
@Service
public class CoinService {

    private final CoinRepository coinRepository;

    public Coin getCoinOrThrow(String symbol) {
        return coinRepository.findBySymbol(symbol)
                .orElseThrow(CoinNotFoundException::new);
    }
}
