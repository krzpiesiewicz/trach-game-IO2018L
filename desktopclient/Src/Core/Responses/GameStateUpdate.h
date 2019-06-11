
#ifndef TRACH_GAMESTATEUPDATE_H
#define TRACH_GAMESTATEUPDATE_H

#include <cpprest/json.h>
#include <Src/Core/GameState.h>

/**
 * response from server with all data about current state of the game
 */
class GameStateUpdate
{
public:
    explicit GameStateUpdate(std::string json)
    {
        auto obj = web::json::value::parse(json);
        gameplayId = obj["gamePlayId"].as_integer();
        updateId = obj["updateId"].as_integer();
        gameState = std::make_shared<GameState>(obj["gameState"]);

        hasPlannedEvaluation = obj.has_object_field("timeOfCommingEvaluation");
        if (hasPlannedEvaluation)
        {
            evaluationTime = obj["timeOfCommingEvaluation"].as_string();
        }
    }

    /**
     * id of gameplay
     */
    int gameplayId;

    /**
     * id of this update
     */
    int updateId;

    /**
     * current gameState
     */
    std::shared_ptr<GameState> gameState;

    /**
     * is any evaluation in plans
     */
    bool hasPlannedEvaluation;

    /**
     * when is evaluation planned or empty string
     */
    std::string evaluationTime;

};


#endif //TRACH_GAMESTATEUPDATE_H
