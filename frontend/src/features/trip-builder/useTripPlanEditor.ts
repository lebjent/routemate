import { useEffect, useState } from 'react';
import { createDayRegion, createSchedule, type DayDescriptor, type DayPlan, type Schedule, type TransportType } from './model';

type RegionField = 'countryCode' | 'regionCode' | 'note';
type ScheduleField = Exclude<keyof Schedule, 'id'>;

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

  const addSchedule = (dayIndex: number, regionIndex: number) => {
    setDayPlans((current) =>
      current.map((plan, currentDayIndex) =>
        currentDayIndex === dayIndex
          ? {
              ...plan,
              regions: plan.regions.map((region, currentRegionIndex) =>
                currentRegionIndex === regionIndex ? { ...region, schedules: [...region.schedules, createSchedule()] } : region
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

  return { dayPlans, updateRegion, addRegion, removeRegion, addSchedule, updateSchedule, removeSchedule };
};
