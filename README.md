# Problem Obiadujących Filozofów 

## Opis
Jest to aplikacja konsolowa napisana w języku Java, pozwalająca na symulację oraz testowanie różnych algorytmów rozwiązujących klasyczny problem ucztujących filozofów (*Dining Philosophers Problem*). Program umożliwia obserwację zachowania wątków podczas rywalizacji o współdzielone zasoby (widelce) oraz analizę wpływu wybranych mechanizmów synchronizacji na unikanie zakleszczeń (*deadlock*) i głodzenia (*starvation*).

### Wersja bazowa
Wariant bez centralnego arbitra, w którym filozofowie podnoszą widelce w losowej kolejności (najpierw lewy lub prawy). W tym wariancie nie ma zaimplementowanego żadnego systemu który przeciwdziałałby pojawieniu się zjawiska zakleszczenia lub zagłodzenia. 

### Wersja z kelnerem
Rozwiązanie wykorzystujące dodatkowy obiekt nadzorujący (`Waiter`), pełniący rolę arbitra z wewnętrzną kolejką FIFO. Kelner pozwala na jednoczesne ubieganie się o widelce maksymalnie $N - 1$ filozofom (gdzie $N$ to liczba filozofów przy stole), co całkowicie eliminuje ryzyko zakleszczenia oraz zapewnia sprawiedliwy dostęp do posiłków w kolejności zgłoszeń (w programie odpowiada jej opcja nr 1).

### Wersja z użyciem Hierarchii
Implementacja oparta na globalnym uporządkowaniu zasobów. Każdy widelec ma przypisany unikalny identyfikator (`id`), a filozofowie zawsze podnoszą najpierw widelec o niższym numerze, a dopiero potem o wyższym. Przełamuje to cykl oczekiwania i zapobiega zakleszczeniom. Hierarchia nie zapobiega zjawisku zagłodzenia. By zapobiec zagłodzeniu zmieniono działania zamków. Zamki (`ReentrantLock`) działają w trybie sprawiedliwym (`fair = true`), co chroni wątki przed głodzeniem (w programie odpowiada jej opcja nr 3).

## Działanie programu 

### Wybór algorytmu
Po uruchomieniu programu użytkownik z poziomu menu konsolowego wybiera strategię synchronizacji, którą chce przetestować.

```bash
Wybierz wersję programu:

1. Wersja z kelnerem (Waiter)
2. Wersja z bazowa
3. Wersja z użyciem Hierarchii

0. Zakończ program
```

### Wprowadzenie parametrów ilości filozofów i długości trwania symulacji
Aplikacja pozwala na dynamiczną konfigurację środowiska testowego przed startem wątków:
* **Liczba filozofów** — określenie liczby wątków oraz odpowiadającej im liczby widelców przy stole (wymagane minimum 2 filozofów).
* **Czas działania programu** — określenie długości trwania symulacji w sekundach ]. Po upływie tego czasu wątki są bezpiecznie przerywane (`interrupt()`).

Przykładowo:

```bash
Podaj liczbę filozofów: 10
Podaj czas działania programu (w sekundach): 1
Uruchamianie wersji 1 na 1.0 sekund...
```

### Wynik końcowy
W trakcie działania program na bieżąco wypisuje komunikaty o stanie każdego filozofa (`myśli`, `je` wraz z numerami używanych widelców). Po zakończeniu odliczania i bezpiecznym zatrzymaniu wszystkich wątków na ekranie wyświetlane jest zbiorcze zestawienie w formacie `ID_Filozofa, Ilość_Zjedzonych_Posiłków`, pozwalające ocenić przepustowość oraz równomierność dostępu do zasobów dla wybranego algorytmu.

Przykładowe informacje wyświetlane podczas działania programu:
```bash
Filozof 9 myśli.
Filozof 0 myśli.
Filozof 5 myśli.
Filozof 1 myśli.
Filozof 7 myśli.
Filozof 2 myśli.
Filozof 8 je (widelce: 8 i 9).
Filozof 1 je (widelce: 1 i 2).
Filozof 8 myśli.
Filozof 7 je (widelce: 7 i 8).
Filozof 1 myśli.
```
Przykładowy wynik działania programu:
```bash
ID_Filozofa, Ilość_Zjedzonych_Posiłków

0  ,1         1  ,1         2  ,1         3  ,1         
4  ,1         5  ,0         6  ,1         7  ,1         
8  ,1         9  ,0         
Symulacja zakończona.
```


