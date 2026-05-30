package com.trip.routemate.lotto.service.impl;

import com.trip.routemate.lotto.service.LottoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
@Service("LottoService")
public class LottoServiceImpl implements LottoService {

    @Override
    public List<Integer> generateLottoNumbers() {
        return new Random().ints(1, 46) // 1 이상 46 미만의 난수 스트림
                .distinct()             // 중복 제거
                .limit(6)               // 6개만 선별
                .sorted()               // 오름차순 정렬
                .boxed()                // Stream<Integer>로 변환
                .toList();              // List로 반환 (Java 16+)
    }

}
