
#ifndef TRACH_INGAMEUI_H
#define TRACH_INGAMEUI_H


#include <QtWidgets/QWidget>
#include <Src/Core/GameState.h>
#include <Src/UI/CurrentTreeTableUI.h>
#include <QtWidgets/QHBoxLayout>
#include <thread>
#include "HandUI.h"
#include "PlayersUI.h"
#include "../ServerConnection.h"
#include "MainTableUI.h"

class InGameUI : public QWidget
{
Q_OBJECT

private:
    PlayerInfoUI* mainPlayerInfo;
    PlayersUI* playersInfo;
    CurrentTreeTableUI* currentTree;
    HandUI* handUI;
    MainTableUI* mainTableUi;
    int playerId;


public:
    bool isUpdating;
    InGameUI(QWidget *parent, GameState *state, int playerId, ServerConnection* connection)
            : QWidget(parent)
    {
        this->playerId = playerId;

        auto mainLayout = new QHBoxLayout(parent);
        auto leftMainLayout = new QVBoxLayout();
        auto horizontalLayout = new QHBoxLayout();
        auto bottomLayout = new QHBoxLayout();
        auto rightBarLayout = new QVBoxLayout();

        playersInfo = new PlayersUI(this, connection);
        rightBarLayout->addLayout(playersInfo->layout);

        auto mainPlayer = state->findPlayerById(playerId);
        handUI = new HandUI(parent, mainPlayer->hand);
        mainPlayerInfo = new PlayerInfoUI(parent, *(mainPlayer), connection);

        bottomLayout->addWidget(mainPlayerInfo, 1);
        bottomLayout->addWidget(handUI, 100);

        currentTree = new CurrentTreeTableUI(this, connection, state, playerId, handUI);
        mainTableUi = new MainTableUI(this, connection);

        horizontalLayout->addWidget(mainTableUi);
        horizontalLayout->addWidget(currentTree);

        leftMainLayout->addLayout(horizontalLayout, 100);
        leftMainLayout->addLayout(bottomLayout, 1);

        mainLayout->addLayout(leftMainLayout, 10);
        mainLayout->addLayout(rightBarLayout, 1);

        setData(state);
    }

public slots:
    void setData(GameState* state)
    {
        playersInfo->setData(state->players);
        currentTree->setData(state);
        handUI->setData(state->findPlayerById(playerId)->hand);
        auto player = state->findPlayerById(playerId);
        if (!state->hasCardTree && state->playerIdOnmove != player->id)
        {
            currentTree->disableThrowingCards();
        } else{
            currentTree->enableThrowingCards();
        }
        mainTableUi->setData(state);
        auto mainPlayer = state->findPlayerById(playerId);
        mainPlayerInfo->setData(*mainPlayer);
        isUpdating = false;
    }
};


#endif //TRACH_INGAMEUI_H
