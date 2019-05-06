
#ifndef TRACH_PLAYERINFOUI_H
#define TRACH_PLAYERINFOUI_H


#include <QtWidgets/QWidget>
#include <QtWidgets/QLabel>
#include <Src/Core/Player.h>
#include <Src/ServerConnection.h>
#include "HealthBarUI.h"

class QDragEnterEvent;
class QDragLeaveEvent;
class QDropEvent;

class PlayerInfoUI : public QWidget
{

private:

    QLabel *nickLabel;
    QLabel *background;
    QLabel *avatar;
    HealthBarUI *healthBarUI;
    ServerConnection* connection;
    int playerId;

public:

    void paintEvent(QPaintEvent *e);

    PlayerInfoUI(QWidget *parent, Player &player, ServerConnection* connection);

    void setData(Player &player);

    void dragLeaveEvent(QDragLeaveEvent *event) override;

    void dragEnterEvent(QDragEnterEvent *event) override;

    void dragMoveEvent(QDragMoveEvent *event) override;

    void dropEvent(QDropEvent *event) override;

};


#endif //TRACH_PLAYERINFOUI_H
