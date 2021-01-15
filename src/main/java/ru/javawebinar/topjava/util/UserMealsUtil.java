package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

        System.out.println("\nMeals filtered by cycles:");
        List<UserMealWithExcess> mealsToFilteredByCycles = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 1000);
        mealsToFilteredByCycles.forEach(System.out::println);

        System.out.println("\nMeals filtered by streams:");
        List<UserMealWithExcess> mealsToFilteredByStreams = filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 1000);
        mealsToFilteredByStreams.forEach(System.out::println);

        System.out.println("\nMeals filtered by cycles, optional2:");
        List<UserMealWithExcess> mealsToFilteredByCyclesOptional2 = Optional2.filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 1000);
        mealsToFilteredByCyclesOptional2.forEach(System.out::println);

        System.out.println("\nMeals filtered by streams, optional2:");
        List<UserMealWithExcess> mealsToFilteredByStreamsOptional2 = Optional2.filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 1000);
        mealsToFilteredByStreamsOptional2.forEach(System.out::println);

    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        List<UserMeal> userMealsAtThisTimeList = new ArrayList<>();
        int calories = 0;
        for (UserMeal userMeal : meals) {
            if (TimeUtil.isBetweenHalfOpen(userMeal.getDateTime().toLocalTime(), startTime, endTime)) {
                userMealsAtThisTimeList.add(userMeal);
                calories += userMeal.getCalories();
            }
        }

        boolean excess = calories > caloriesPerDay;
        List<UserMealWithExcess> userMealsWithExcessList = new ArrayList<>();
        for (UserMeal userMeal : userMealsAtThisTimeList) {
            UserMealWithExcess userMealWithExcess = new UserMealWithExcess(
                    userMeal.getDateTime(),
                    userMeal.getDescription(),
                    userMeal.getCalories(),
                    excess);
            userMealsWithExcessList.add(userMealWithExcess);
        }

        return userMealsWithExcessList;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

        AtomicInteger calories = new AtomicInteger(0);
        List<UserMeal> userMealsAtThisTimeList = meals.stream()
                .filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                .peek(meal -> calories.addAndGet(meal.getCalories()))
                .collect(Collectors.toList());

        List<UserMealWithExcess> userMealsWithExcessList = new ArrayList<>();
        boolean excess = calories.get() > caloriesPerDay;
        userMealsAtThisTimeList.forEach(meal -> {
            UserMealWithExcess userMealWithExcess = new UserMealWithExcess(
                    meal.getDateTime(),
                    meal.getDescription(),
                    meal.getCalories(),
                    excess);
            userMealsWithExcessList.add(userMealWithExcess);
        });

        return userMealsWithExcessList;
    }

    public static class Optional2 {

        public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

            int calories = 0;
            List<UserMealWithExcess> userMealsWithExcessList = new ArrayList<>();
            AtomicBoolean excess = new AtomicBoolean();
            for (UserMeal userMeal : meals) {
                if (TimeUtil.isBetweenHalfOpen(userMeal.getDateTime().toLocalTime(), startTime, endTime)) {
                    calories += userMeal.getCalories();

                    UserMealWithExcess userMealWithExcess = new UserMealWithExcess(
                            userMeal.getDateTime(),
                            userMeal.getDescription(),
                            userMeal.getCalories(),
                            excess);
                    userMealsWithExcessList.add(userMealWithExcess);
                }
            }

            excess.set(calories > caloriesPerDay);

            return userMealsWithExcessList;
        }

        public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {

            AtomicInteger calories = new AtomicInteger(0);
            AtomicBoolean excess = new AtomicBoolean();
            List<UserMealWithExcess> userMealsWithExcessList = meals.stream()
                    .filter(meal -> TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime))
                    .map(meal -> {
                        calories.addAndGet(meal.getCalories());
                        return new UserMealWithExcess(
                                meal.getDateTime(),
                                meal.getDescription(),
                                meal.getCalories(),
                                excess);
                    })
                    .collect(Collectors.toList());

            excess.set(calories.get() > caloriesPerDay);

            return userMealsWithExcessList;
        }
    }
}
