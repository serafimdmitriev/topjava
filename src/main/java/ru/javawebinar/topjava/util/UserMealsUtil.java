package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class UserMealsUtil {

    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        LocalTime from = LocalTime.of(7, 0);
        LocalTime till = LocalTime.of(12, 0);
        int caloriesPerDay = 2000;

        System.out.println("\nMeals filtered by cycles:");
        List<UserMealWithExcess> mealsToFilteredByCycles = filteredByCycles(meals, from, till, caloriesPerDay);
        mealsToFilteredByCycles.forEach(System.out::println);

        System.out.println("\nMeals filtered by streams:");
        List<UserMealWithExcess> mealsToFilteredByStreams = filteredByStreams(meals, from, till, caloriesPerDay);
        mealsToFilteredByStreams.forEach(System.out::println);

        System.out.println("\nMeals filtered by cycles, optional2:");
        List<UserMealWithExcess> mealsToFilteredByCyclesOptional2 = Optional2.filteredByCycles(meals, from, till, caloriesPerDay);
        mealsToFilteredByCyclesOptional2.forEach(System.out::println);

        System.out.println("\nMeals filtered by streams, optional2:");
        List<UserMealWithExcess> mealsToFilteredByStreamsOptional2 = Optional2.filteredByStreams(meals, from, till, caloriesPerDay);
        mealsToFilteredByStreamsOptional2.forEach(System.out::println);

    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        List<UserMeal> userMealsAtThisTimeList = new ArrayList<>();
        Map<LocalDate, Integer> caloriesThisDay = new HashMap<>();

        for (UserMeal meal : meals) {
            caloriesThisDay.put(meal.getDateTime().toLocalDate(),
                    caloriesThisDay.getOrDefault(meal.getDateTime().toLocalDate(), 0) + meal.getCalories());

            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                userMealsAtThisTimeList.add(meal);
            }
        }

        List<UserMealWithExcess> userMealsWithExcessList = new ArrayList<>();
        for (UserMeal meal : userMealsAtThisTimeList) {
            UserMealWithExcess userMealWithExcess = new UserMealWithExcess(
                    meal.getDateTime(),
                    meal.getDescription(),
                    meal.getCalories(),
                    caloriesThisDay.get(meal.getDateTime().toLocalDate()) > caloriesPerDay);
            userMealsWithExcessList.add(userMealWithExcess);
        }

        return userMealsWithExcessList;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        Map<LocalDate, Integer> caloriesThisDay = new HashMap<>();
        List<UserMeal> userMealsAtThisTimeList = meals.stream()
                .peek(meal ->
                    caloriesThisDay.put(meal.getDateTime().toLocalDate(),
                            caloriesThisDay.getOrDefault(meal.getDateTime().toLocalDate(), 0) + meal.getCalories()))
                .filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                .collect(Collectors.toList());

        return userMealsAtThisTimeList.stream()
                .map(meal -> new UserMealWithExcess(
                    meal.getDateTime(),
                    meal.getDescription(),
                    meal.getCalories(),
                    caloriesThisDay.get(meal.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }

    public static class Optional2 {

        public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

            List<UserMealWithExcess> userMealsWithExcessList = new ArrayList<>();
            Map<LocalDate, Integer> caloriesThisDay = new HashMap<>();
            Map<LocalDate, AtomicBoolean> isDayWithExcess = new HashMap<>();

            for (UserMeal meal : meals) {

                LocalDate localDate = meal.getDateTime().toLocalDate();
                caloriesThisDay.put(localDate,
                        caloriesThisDay.getOrDefault(localDate, 0) + meal.getCalories());

                isDayWithExcess.putIfAbsent(localDate, new AtomicBoolean(false));
                if (caloriesThisDay.get(localDate) > caloriesPerDay)
                    isDayWithExcess.get(localDate).set(true);

                if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {

                    UserMealWithExcess userMealWithExcess = new UserMealWithExcess(
                            meal.getDateTime(),
                            meal.getDescription(),
                            meal.getCalories(),
                            isDayWithExcess.get(localDate));
                    userMealsWithExcessList.add(userMealWithExcess);
                }
            }

            return userMealsWithExcessList;
        }

        public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

            Map<LocalDate, Integer> caloriesThisDay = new HashMap<>();
            Map<LocalDate, AtomicBoolean> isDayWithExcess = new HashMap<>();

            return meals.stream()
                    .peek(meal -> {
                        LocalDate localDate = meal.getDateTime().toLocalDate();
                        caloriesThisDay.put(localDate,
                                caloriesThisDay.getOrDefault(localDate, 0) + meal.getCalories());

                        isDayWithExcess.putIfAbsent(localDate, new AtomicBoolean(false));
                        if (caloriesThisDay.get(localDate) > caloriesPerDay)
                            isDayWithExcess.get(localDate).set(true);
                    })
                    .filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                    .map(meal -> new UserMealWithExcess(
                            meal.getDateTime(),
                            meal.getDescription(),
                            meal.getCalories(),
                            isDayWithExcess.get(meal.getDateTime().toLocalDate())))
                    .collect(Collectors.toList());
        }
    }
}
