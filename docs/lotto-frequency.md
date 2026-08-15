# Lotto frequency draw

The `GET /api/lotto/frequent-numbers` endpoint creates a six-number combination
from the 15 most frequently drawn Lotto 6/45 winning numbers.

## Calculation rules

- Only the six main winning numbers are counted; bonus numbers are excluded.
- Each combination is a random, non-duplicated selection from the configured
  top-number group and is sorted before it is returned.
- Historical frequency does not change the probability of a future draw.

## Data and caching

The default history URL is the official Donghaeng Lottery result endpoint. It
returns ten draw results per request, so RouteMate retrieves the history in
ten-draw pages and caches the calculated result in the application for 12 hours.

```bash
LOTTO_HISTORY_SOURCE_URL=https://example.com/lotto-history.json
LOTTO_HISTORY_REFRESH_INTERVAL=12h
LOTTO_HISTORY_TOP_NUMBER_COUNT=15
```

The default source is official. Change `LOTTO_HISTORY_SOURCE_URL` only when a
separate, reviewed data provider is required.
