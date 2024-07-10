### Thread

1. Click `Threads` to display threads related content.
   > ![ThreadsSample01.png](./ThreadsSample01.png)
2. Click `Show table` to show Threads table.
3. Check the 3 related threads in the table.
   1. The start time and end time is same as expected.
4. Check the 3 related threads in the chart.
   1. When the thread is running, it's color is green.
   2. When the thread is blocked or sleep. it has different color.
   3. The `main-synchronizedSleep01` thread start before `main-synchronizedSleep02`, 
      but the `main-synchronizedSleep02` get the lock first.
   4. The `main` thread ends before the 2 threads start.
   > ![ThreadsSample02.png](./ThreadsSample02.png)
5. Click `main-synchronizedSleep001` to show more information.
6. There a 2 event types: `Java Monitor Blocked` and `Java Thread Sleep`.
7. There are 2 Samples in the thread.
   > ![ThreadsSample03.png](./ThreadsSample03.png)
8. Click `Edit Thread Lanes` icon.
9. Here you can choose which event types to show in each lane.
10. `Java Monitor Blocked` event `Java Thread Sleep` event are selected to ben shown.
11. The event count and event color are same as expected.

