#ifndef DRAGWIDGET_H
#define DRAGWIDGET_H

#include <QFrame>
#include <QLabel>
#include <Src/ServerConnection.h>
#include "Src/Core/GameState.h"
#include "CurrentCardTreeUI.h"
#include "HandUI.h"
#include <QtGui/QPainter>

class QDragEnterEvent;
class QDropEvent;

class CurrentTreeTableUI : public QFrame
{
public:
    explicit CurrentTreeTableUI(QWidget *parent, ServerConnection* connection, GameState* currentState, int playerId, HandUI* handUI);
    void setData(GameState* gameState);

    void paintEvent( QPaintEvent *e )
    {
        QPainter painter(this);
        painter.setPen(Qt::blue);
        painter.setFont(QFont("Arial", 30));
        painter.drawText(rect(), Qt::AlignCenter, canMakeMove? "Rzuć karty tutaj" : "Czekaj na swoją turę");
    }

    void disableThrowingCards()
    {
        setAcceptDrops(false);
        canMakeMove = false;
    }

    void enableThrowingCards()
    {
        setAcceptDrops(true);
        canMakeMove = true;
    }

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
    bool canMakeMove;
};

#endif // DRAGWIDGET_H