
#ifndef TRACH_PLAYERINFOUI_H
#define TRACH_PLAYERINFOUI_H


#include <QtWidgets/QWidget>
#include <QtWidgets/QLabel>
#include <Src/Core/Player.h>
#include <Src/ServerConnection.h>
#include "HealthBarUI.h"

class QDragEnterEvent;
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

    PlayerInfoUI(QWidget *parent, Player &player, ServerConnection* connection)
            : QWidget(parent)
    {
        this->connection = connection;
        playerId = player.id;
        setMinimumWidth(200);
        setMinimumHeight(100);

        resize(200, 100);
        background = new QLabel(this);
        QImage image;
        image = QImage(200, 100, QImage::Format_ARGB32);
        image.fill(qRgb(220, 220, 220));
        background->setPixmap(QPixmap::fromImage(image));

        nickLabel = new QLabel(this);
        nickLabel->resize(300, 50);

        avatar = new QLabel(this);
        avatar->move(140, 0);

        healthBarUI = new HealthBarUI(this);
        healthBarUI->move(0, 50);

        setData(player);
        setAcceptDrops(true);
    }

    void setData(Player &player)
    {
        nickLabel->setText(QString::fromStdString(player.name));
        healthBarUI->setHealthValue(player.health);
        auto avatarPath = ":/Assets/avatar" + to_string(1 + (player.id % 5)) + ".png";
        avatar->setPixmap(QPixmap(QString::fromStdString(avatarPath)).scaled(60, 100,
                                                                             Qt::KeepAspectRatio,
                                                                             Qt::SmoothTransformation));

    }

    void dragEnterEvent(QDragEnterEvent *event) override;

    void dragMoveEvent(QDragMoveEvent *event) override;

    void dropEvent(QDropEvent *event) override;
};


#endif //TRACH_PLAYERINFOUI_H
