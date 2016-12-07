# podiliardino

Podiliardino is a project aimed to organized tournaments. It allows to manage players, teams, tournaments, days of tournaments, matches and rankings.
Right now it supports only Swiss tournament type with "CSV" export type. 

# Features

Podiliardino divide itself into 4 features:

1. Player management
2. Team management
3. Tournament management
4. Day management


There are several main restrictions:
 * 2 players can fund a team and a player can fund multiple teams;
 * A team can partecipate into several tournament and a tournament can receive partecipations by multiple teams;
 * A tournament is composed by several "days" but a day is related to a single tournament;
 * 2 teams match eachother inside a single day; 

## Player Management

A player is the base of the software. In the "Player Management" you can add, change data and remove players. Note that removing a player will remove all the 
data that directly or indirectly reference said player: so use with caution.

## Team Management

A team is created by 2 **different** players. Just like with players, teams can be created, changed or removed: if this isthe case, all the data which reference the team will be delete; Player, since are the bricks to build teams are not though.

## Tournament Management

In the "Tournament Management" you can handle the tournaments. You can create new tournament and add teams inside them. You can add as many teams as you want, however, you can't add teams sharing the same people: a player can join a tournament within a single team.

The software uses the swiss tournament mode and currently it can't change it behaviour. In the right panel you can see how many days you need to complete the tournament. Of course you may add additional days as your leasure: for example if the tournament should last 5 days you can add a sixth one just to add more matches among the teams. The software lets you do it. In summary the days expected in the right panel are just a **suggestion**, nothign more, nothing less.

In the tab you can add, change and remove tournament. If you remove a tournament, all its data will be deleted: hence days, matches and matches results. Teams and Players won't be affected though.

## Day Management

Almost all the feature podiliardino has can be found in this tab. The tab lets you handle the data of a single day, that includes:
 * add, edit and removal of a day;
 * generates the matches of the day;
 * save the list of match to do;
 * set the result of a match;
 * generate the ranking among the teams using the swiss mode;
 
 