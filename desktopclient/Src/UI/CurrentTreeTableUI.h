#ifndef DRAGWIDGET_H
#define DRAGWIDGET_H

#include <QFrame>
#include <QLabel>
#include <Src/ServerConnection.h>
#include "Src/Core/GameState.h"
#include "CurrentCardTreeUI.h"
#include "HandUI.h"

class QDragEnterEvent;
class QDropEvent;

class CurrentTreeTableUI : public QFrame
{
public:
    explicit CurrentTreeTableUI(QWidget *parent, ServerConnection* connection, GameState* currentState, int playerId, HandUI* handUI);
    void setData(GameState* gameState);

protected:

    void dragEnterEvent(QDragEnterEvent *event) override;

    void dragMoveEvent(QDragMoveEvent *event) override;

    void dropEvent(QDropEvent *event) override;

    void mousePressEvent(QMouseEvent *event) override;

private:
    int playerId;
    QLabel* label;
    ServerConnection* connection;
    GameState* currentState;
    CurrentCardTreeUI* currentTree;
};

#endif // DRAGWIDGET_H