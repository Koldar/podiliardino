# Podiliardino

Podiliardino is a project aimed to organized tournaments. It allows to manage players, teams, tournaments, days of tournaments, matches and rankings.
Right now it supports only Swiss tournament type with "CSV" export type. 

# Program feature description

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
 
After selecting a tournament in the left pane, you can add/change or remove days. Removing days will delete all the data associated to that day; however, players, teams and tournaments won't be affected at all.

After selecting a day, you may generate the matches of that day. You can generate matches only if at least 2 teams joined the tournament. By pressing "Generate matches" the software will generate the matches according to the "swiss tournament mode". You can press as many times you want such button: the computer will recompute new matches according to the swiss mode. The computer will also take care of the "bye": in case of odd number of partecipants, the tournament will assign a bye to one team. Team bye assignment follows "swiss mode" rules. 

Bying Teams will have a fake match already done. Viceversa, real matches will need to use the button "Update match result" in order to be declared "done".
You can also update a match result as many time as you want: the software will update the result accordingly. However, once you update the match for the first time, follows update won't affect its status (it will remain set to "done").

### Exporting useful information

While you compile the match results of the day, you can press **anytime** "export ranking" and "export matches": the former will gneerate a CSV containing the ranking of all the teams (updated to the matches you've set) whilst the latter will also export a CSV containing all the matches that "needs to be done". You can't export matches if the day is already completed. 

### Tournament Advancement

After you totally completed a day, you may add another day in the same tournament. You may not add a day in a tournament if at least one day in the same tourney has still a match to be done.

# Swiss Tournament mode

Podiliardino uses swiss tournament modes. This particularly tournament mode allows to maximize the number of matches inside a tournament,
therefore allowing to maximize the fun. But what does that means? Here's the rules to build a swiss complliant tournament:

	1. Swiss tournaments accept whatever number of partecipants, both even or odd;
	2. The tournament is divided in levels (or days). In every level the first team in the ranking fights against the seconds best, the third against the fourth, and so forth;
	3. Every winner of a match is awarded with 3 points while every loser gets 0 points;
	4. If the tournament has a odd number of partecipants, the unpaired one will "bye": this means that a fake match with a goals result set to 10/9 (unpaired team scored 10 goals with a dummy team and received 9 goals) will be created;
	5. A team can bye only once in the tournament;
	6. The team that will bye is the first team we encounter scrolling up from the bottom of the ranking which hasn't byed yet;
	7. A team can't fight the same opponent twice in the same tournament.

The ranking is computed in the following way:

In the first day, the teams are allocated in the ranking randomly: however, each of them will fight according to the rules said before. But from the second day onwards the ranking can be computed in the following way:

	1. team X is ranked higher than Y if X has more points than Y;
	2. if the points are the same, team X is ranked higher than Y if the differences of goals of X (goals scored in the tournament subtracted to goals received in the tournament) is greater than Y;
	3. if the differences of goals are the same, team X is ranked higher than Y if the goals scored in the tournament by X are greater than Y;
	4. if the goals scored are the same, team X is ranked higher than Y if the number of goals scored by every opponent of X is greater than the number of goals scored by every opponent of Y.  

 
 
 