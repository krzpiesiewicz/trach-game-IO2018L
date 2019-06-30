
#ifndef TRACH_PLAYERSUI_H
#define TRACH_PLAYERSUI_H

#include <vector>
#include <QtWidgets/QWidget>
#include <Src/Core/Player.h>
#include <QtWidgets/QVBoxLayout>
#include "PlayerInfoUI.h"
/**
 * QWidget showing all players that are currently in game
 */
class PlayersUI : public QWidget
{
Q_OBJECT
public:

    /**
     * layout this widget is attached to
     */
    QVBoxLayout *layout = new QVBoxLayout();

    /**
     *
     * @param parent parent of widget
     * @param connection connection to server
     */
    PlayersUI(QWidget* parent, ServerConnection* connection) : QWidget(parent)
    {
        this->connection = connection;
    }

    /**
     * Updates data of widget
     * @param players updated players information
     */
    void setData(std::vector<Player>players)
    {
        for (int i = 0; i < (int)playersInfo.size();i++)
        {
            layout->removeWidget(playersInfo[i]);
            delete playersInfo[i];
        }
        if (emptySpace != nullptr)
        {
            layout->removeWidget(emptySpace);
            delete emptySpace;
        }

        playersInfo.clear();

        for (int i=0;i<(int)players.size();i++)
        {
            playersInfo.emplace_back(new PlayerInfoUI(this, players[i], connection));
            playersInfo[i]->move(0, i * 100);
            layout->addWidget(playersInfo[i], 1);
        }
        emptySpace = new QWidget(this);
        layout->addWidget(emptySpace, 100);

    }

private:

    std::vector<PlayerInfoUI*>playersInfo;

    QWidget* emptySpace = nullptr;

    ServerConnection* connection;
};

#endif //TRACH_PLAYERSUI_H
