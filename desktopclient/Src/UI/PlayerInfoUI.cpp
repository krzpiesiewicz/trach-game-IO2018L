
#include <QtWidgets>
#include <Src/Core/CardTreeInternalNode.h>

#include "PlayerInfoUI.h"

PlayerInfoUI::PlayerInfoUI(QWidget *parent, Player &player, ServerConnection *connection)
        : QWidget(parent)
{
    this->connection = connection;
    playerId = player.id;
    setMinimumWidth(200);
    setMinimumHeight(100);
    resize(200, 100);

    background = new QLabel(this);
    QImage backgroundImage;
    backgroundImage = QImage(196, 96, QImage::Format_ARGB32);
    backgroundImage.fill(qRgb(200, 200, 200));
    background->setPixmap(QPixmap::fromImage(backgroundImage));
    background->move(2,2);

    nickLabel = new QLabel(this);
    nickLabel->resize(300, 50);
    nickLabel->move(5, 0);

    avatar = new QLabel(this);
    avatar->move(140, 0);

    healthBarUI = new HealthBarUI(this);
    healthBarUI->move(0, 50);

    setData(player);
    setAcceptDrops(true);
}

void PlayerInfoUI::setData(Player &player)
{
    nickLabel->setText(QString::fromStdString(player.name));
    healthBarUI->setHealthValue(player.health);
    auto avatarPath = ":/Assets/avatar" + to_string(1 + (player.id % 5)) + ".png";
    avatar->setPixmap(QPixmap(QString::fromStdString(avatarPath)).scaled(60, 100,
                                                                         Qt::KeepAspectRatio,
                                                                         Qt::SmoothTransformation));

}

void PlayerInfoUI::paintEvent(QPaintEvent *e)
{
    QPainter painter(this);

    QPen pen;
    pen.setWidth(4);

    painter.setPen(pen);
    painter.setFont(QFont("Arial", 30));
    painter.drawRect(0,0, 200, 100);

}

void PlayerInfoUI::dragLeaveEvent(QDragLeaveEvent *event)
{
    QImage backgroundImage;
    backgroundImage = QImage(196, 96, QImage::Format_ARGB32);
    backgroundImage.fill(qRgb(200, 200, 200));
    background->setPixmap(QPixmap::fromImage(backgroundImage));
}

void PlayerInfoUI::dragEnterEvent(QDragEnterEvent *event)
{
    if (event->mimeData()->hasFormat("cardTreeToPlay"))
    {
        event->acceptProposedAction();

        QImage backgroundImage;
        backgroundImage = QImage(196, 96, QImage::Format_ARGB32);
        backgroundImage.fill(qRgb(240, 240, 240));
        background->setPixmap(QPixmap::fromImage(backgroundImage));

    } else
    {
        event->ignore();
    }
}

void PlayerInfoUI::dragMoveEvent(QDragMoveEvent *event)
{
    if (event->mimeData()->hasFormat("cardTreeToPlay"))
    {
        event->acceptProposedAction();
    } else
    {
        event->ignore();
    }
}

void PlayerInfoUI::dropEvent(QDropEvent *event)
{
    if (event->mimeData()->hasFormat("cardTreeToPlay"))
    {

        QByteArray itemData = event->mimeData()->data("cardTreeToPlay");
        QDataStream dataStream(&itemData, QIODevice::ReadOnly);

        QPoint offset;
        long long addrRaw;
        dataStream >> offset >> addrRaw;
        CardTreeInternalNode *tree;
        tree = reinterpret_cast<CardTreeInternalNode *>(addrRaw);
        connection->playCardAtPlayer(tree, playerId);

        QImage backgroundImage;
        backgroundImage = QImage(196, 96, QImage::Format_ARGB32);
        backgroundImage.fill(qRgb(200, 200, 200));
        background->setPixmap(QPixmap::fromImage(backgroundImage));

        event->acceptProposedAction();

    } else
    {
        event->ignore();
    }
}