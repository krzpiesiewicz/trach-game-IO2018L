 # game module

The module contains definitions of game objects and it is responsible for game management.

Contents:
1. [Basics](https://github.com/krzpiesiewicz/trach-scala#basics)
   - [Actions](https://github.com/krzpiesiewicz/trach-scala#actions)
   - [Cards](https://github.com/krzpiesiewicz/trach-scala#cards)
   - [Characters](https://github.com/krzpiesiewicz/trach-scala#characters)
   - [Attributes](https://github.com/krzpiesiewicz/trach-scala#attributes)
2. [Module architecture](https://github.com/krzpiesiewicz/trach-scala#module_architecture)
   - [Attributes architecture](https://github.com/krzpiesiewicz/trach-scala#attributes_architecture)
  

## Basics

Trach is a turn-based game which involves special cards. Every player plays as character that has some value of health points. The main goal is to kill other players' characters. The game architecture is based on playing cards.

### Actions

An action is an abstraction for something which changes the game state. Actions are based on played cards. Example actions:
- hurting character,
- changing character's feature,
- modifying the game environment.

### Cards

The main cards categorization:
- **Cards for building an action** - they can be played to create an action.
- **Cards for modifying an action** - they can be played when action caused by an action builder card is going to be applied. The simple transform an action to another one. For instance, an action of hurting some character can be deactivated by the defense card or the target of the action could be changed.


The categorization of cards for building actions:
- **Cards for building an offensive action** - the goal of this kind of cards is to hurt characters.
- **Cards of characters' features or artifacts** - they can be a played by player to its character or other players' characters. When this kind of card is played, it is pinned to the character on the table and adds or modifies character's features.
- **Card modifying the global environment** - they can be played to create special circumstances in which the game will be continued. At the time the card influences the game, it is placed on the table.

### Characters

A Character is an object by which the player can influence the game.

### Attributes

Attributes are abstractions which describes the state of game.

There are two main kinds of attributes:
  - **Characters' attributes** - describing the character. Every character has a set of attributes.
  - **Global attributes** - describing the whole game (not connected with the certain character).

Attributes are either default or can be results of played cards. In the second case 

Basic characters' attributes:
  - **Health** - it is described by two values:
    * current number of health points,
    * maximal number of health points.
    Dropping the number of health points to zero results in the character's death.
  - **Hand** - cards in player's hand.
  - **Active cards** - list of the cards of features or artifacts played on character (only those which influence character attributes).

Basic global attributes:
  - **Stack with covered cards** - stack with shuffled cards from which characters take cards in order to refill the set of cards in hand.
  - **Stack with discarded cards** - stack with cards which were used and they are not active anymore.
  - **Global active cards** - list of the cards which influence the game at the time.
  - **Circle of characters and information whoose turn it is** - characters are seated in the circle. The default turn order is clockwise.

## Module architecture

### Attributes architecture

`trait Attribute`
- `trait GlobalAttribute extends Attribute`
- `trait CharacterAttribute extends Attribute`

Basic characters' attributes:
  - `trait Health extends CharacterAttribute` - __TODO__.
  - `trait Hand extends CharacterAttribute` - __TODO__.
  - `trait CharacterActiveCards extends CharacterAttribute` - __TODO__.
  - `trait TargetChooser extends CharacterAttribute` - __TODO__.

Basic global attributes:
  - `trait CoveredCardsStack extends GlobalAttribute` - __TODO__.
  - `DiscardedCardsStack extends GlobalAttribute` - __TODO__.
  - `GlobalActiveCards extends GlobalAttribute` - __TODO__.
  - `Characters extends GlobalAttribute` - __TODO__.
  - `RoundsManager extends GlobalAttribute` - __TODO__.
