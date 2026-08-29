import { useEffect, useState } from 'react';
import { createDayRegion, createSchedule, type DayDescriptor, type DayPlan, type Schedule, type TransportType } from './model';

type RegionField = 'countryCode' | 'regionCode' | 'note';
type ScheduleField = Exclude<keyof Schedule, 'id' | 'productOrderId' | 'productOrderNo'>;

/**
 * 여행 계획 편집 화면의 중첩 상태와 변경 함수를 한곳에 모은 훅이다.
 *
 * 일차·지역·세부 일정·준비물 추가/삭제 및 필드 변경을 불변 방식으로 처리해 CreateTrip 화면은
 * 표현과 저장 흐름에 집중할 수 있다.
 */
export const useTripPlanEditor = (days: DayDescriptor[]) => {
  const [dayPlans, setDayPlans] = useState<DayPlan[]>([]);

  useEffect(() => {
    setDayPlans((current) => {
      const plansByDate = new Map(current.map((plan) => [plan.date, plan]));
      return days.map((day) => ({ ...day, regions: plansByDate.get(day.date)?.regions ?? [] }));
    });
  }, [days]);

  const updateRegion = (dayIndex: number, regionIndex: number, field: RegionField, value: string) => {
    setDayPlans((current) =>
      current.map((plan, currentDayIndex) => {
        if (currentDayIndex !== dayIndex) return plan;
        return {
          ...plan,
          regions: plan.regions.map((region, currentRegionIndex) => {
            if (currentRegionIndex !== regionIndex) return region;
            return field === 'countryCode'
              ? { ...region, countryCode: value, regionCode: '' }
              : { ...region, [field]: value };
          }),
        };
      })
    );
  };

  const addRegion = (dayIndex: number) => {
    setDayPlans((current) =>
      current.map((plan, index) => (index === dayIndex ? { ...plan, regions: [...plan.regions, createDayRegion()] } : plan))
    );
  };

  const removeRegion = (dayIndex: number, regionIndex: number) => {
    setDayPlans((current) =>
      current.map((plan, index) =>
        index === dayIndex ? { ...plan, regions: plan.regions.filter((_, itemIndex) => itemIndex !== regionIndex) } : plan
      )
    );
  };

  const addSchedule = (dayIndex: number, regionIndex: number, initialValues: Partial<Omit<Schedule, 'id'>> = {}) => {
    setDayPlans((current) =>
      current.map((plan, currentDayIndex) =>
        currentDayIndex === dayIndex
          ? {
              ...plan,
              regions: plan.regions.map((region, currentRegionIndex) =>
                currentRegionIndex === regionIndex
                  ? { ...region, schedules: [...region.schedules, { ...createSchedule(), ...initialValues }] }
                  : region
              ),
            }
          : plan
      )
    );
  };

  const updateSchedule = (dayIndex: number, regionIndex: number, scheduleIndex: number, field: ScheduleField, value: string) => {
    setDayPlans((current) =>
      current.map((plan, currentDayIndex) =>
        currentDayIndex === dayIndex
          ? {
              ...plan,
              regions: plan.regions.map((region, currentRegionIndex) =>
                currentRegionIndex === regionIndex
                  ? {
                      ...region,
                      schedules: region.schedules.map((schedule, currentScheduleIndex) => {
                        if (currentScheduleIndex !== scheduleIndex) return schedule;
                        return field === 'transportType'
                          ? { ...schedule, transportType: value as TransportType }
                          : { ...schedule, [field]: value };
                      }),
                    }
                  : region
              ),
            }
          : plan
      )
    );
  };

  const removeSchedule = (dayIndex: number, regionIndex: number, scheduleIndex: number) => {
    setDayPlans((current) =>
      current.map((plan, currentDayIndex) =>
        currentDayIndex === dayIndex
          ? {
              ...plan,
              regions: plan.regions.map((region, currentRegionIndex) =>
                currentRegionIndex === regionIndex
                  ? { ...region, schedules: region.schedules.filter((_, itemIndex) => itemIndex !== scheduleIndex) }
                  : region
              ),
            }
          : plan
      )
    );
  };

  const replaceDayPlans = (plans: DayPlan[]) => setDayPlans(plans);

  return { dayPlans, updateRegion, addRegion, removeRegion, addSchedule, updateSchedule, removeSchedule, replaceDayPlans };
};
