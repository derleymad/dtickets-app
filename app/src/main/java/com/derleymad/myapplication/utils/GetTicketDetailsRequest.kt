package com.derleymad.myapplication.utils

import android.accounts.NetworkErrorException
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.derleymad.myapplication.R
import com.derleymad.myapplication.TicketActivity
import com.derleymad.myapplication.adapter.TicketMensagemAdapter
import com.derleymad.myapplication.model.Mensagem
import com.derleymad.myapplication.model.Ticket
import com.derleymad.myapplication.model.TicketDetail
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.util.concurrent.Executors

class GetTicketDetailsRequest(private val callback: TicketActivity){

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    lateinit var responseCode : String

    interface Callback{
        fun onPreExecute()
        fun onResult(ticket:TicketDetail)
        fun onFailure(message: String)
    }

    fun execute(username:String, password:String,id : String){
        lateinit var ticketDetail : TicketDetail

        //TODO olhar em baixo
        //Checar no pre execute se tem ou nao internet
        callback.onPreExecute()
        executor.execute{
            try{
                val ticketsAbertos = loginAndGetTicket(username,password,id)
                if(responseCode != "200"){
                    throw NetworkErrorException(callback.getString(R.string.server_off))
                }
                handler.post{callback.onResult(ticketsAbertos)}

            }catch (e: NetworkErrorException){
                val message = e.message ?: callback.getString(R.string.uknown_error)
                Log.e("Teste",message,e)
//                handler.post { callback.onFailure(message)  }
            }finally {
            }

        }
        //FIM DO EXECUTORS
    }

    fun loginAndGetTicket(username: String,password: String, id: String) : TicketDetail{
        val url = "https://atendimento.ufca.edu.br/scp/tickets.php?id=$id"
        val msgs = mutableListOf<Mensagem>()

        val loginForm =
            Jsoup.connect(url)
                .method(Connection.Method.GET)
                .execute()
        responseCode = loginForm.statusCode().toString()
        val doc: Document = Jsoup.connect(url)
            .data("userid", "$username")
            .data("passwd", "$password")
            .cookies(loginForm.cookies())
            .post()

        val page= Jsoup
            .connect(url)
            .cookies(loginForm.cookies())
            .get()

        val content : Elements = page.select("#content")

        val numeroTicket = content
            .select("table")[0]
            .select("tbody")
            .select("tr").select("td")[0].select("h2").select("a").text()

        val firstTableLeft = content
            .select("table")[1].select("tbody").select("tr").select("td:nth-child(1)")

        val firstTableRight = content
            .select("table")[1].select("tbody").select("tr").select("td:nth-child(2)")

        val secondTable = content
            .select(".ticket_info")[1]

        val secondTableLeft= secondTable.select("tbody").select("tr").select("td:nth-child(1)")
        val secondTableRight = secondTable.select("tbody").select("tr").select("td:nth-child(2)")

        val thirdTable = content.select(".ticket_info")[2]

        val descricao = content.select(">h2").text()
        val id = content.select("table").select("tbody").select("tr").select("td").select("h2").select("a").attr("href")
        val status = firstTableLeft.select("table").select("tbody").select("tr")[0].select("td").text()
        val prioridade = firstTableLeft.select("table").select("tbody").select("tr")[1].select("td").text()
        val setor = firstTableLeft.select("table").select("tbody").select("tr")[2].select("td").text()
        val dataCriacao = firstTableLeft.select("table").select("tbody").select("tr")[3].select("td").text()
        val email = firstTableRight.select("table").select("tbody").select("tr")[1].select("td").select("span").text()
        val numero = firstTableRight.select("table").select("tbody").select("tr")[2].select("td").select("span").text()
        val para = secondTableLeft.select("table").select("tbody").select("tr")[0].select("td").text()
        val slaPlan = secondTableLeft.select("table").select("tbody").select("tr")[1].select("td").text()
        val dueDate = secondTableLeft.select("table").select("tbody").select("tr")[2].select("td").text()
        val ultimaMensagem = secondTableRight.select("table").select("tbody").select("tr")[1].select("td").text()
        val ultimaResposta = secondTableRight.select("table").select("tbody").select("tr")[2].select("td").text()
        val servicos = thirdTable.select("tbody").select("tr").select("td").select("table").select("tbody").select("tr")[0].select("td").text()
        val campus = thirdTable.select("tbody").select("tr").select("td").select("table").select("tbody").select("tr")[1].select("td").text()
        val sala = thirdTable.select("tbody").select("tr").select("td").select("table").select("tbody").select("tr")[2].select("td").text()
        val bloco = thirdTable.select("tbody").select("tr").select("td").select("table").select("tbody").select("tr")[3].select("td").text()
        val setorSolicitante = thirdTable.select("tbody").select("tr").select("td").select("table").select("tbody").select("tr")[4].select("td").text()
        val nome = thirdTable.select("tbody").select("tr").select("td").select("table").select("tbody").select("tr")[5].select("td").text()

        val qtdTickets = content.select("#threads").select("li").select("a").text()
        val tableMsg : Elements = content.select("#ticket_thread").select("table")

        for (i in tableMsg){
            val data = i.select("tbody").select("tr")[0].select("th").select("div").select("span")[0].wholeText()
            val status = i.select("tbody").select("tr")[0].select("th").select("div").select("span")[1].wholeText()
            val de = i.select("tbody").select("tr")[0].select("th").select("div").select("span")[2].select(".tmeta.faded.title").text()
            val mensagem = i.select("tbody").select("tr")[1].select("td").select("div").text()
            msgs.add(
                Mensagem(
                    data = data,
                    status = status,
                    de = de,
                    mensagem = mensagem)
            )
        }

        var ticketDetail = TicketDetail(
            id = id.substring(15..20),
            descricao = descricao,
            status = status,
            prioridade = prioridade,
            setor = setor,
            dataCriacao = dataCriacao,
            email = email,
            numeroTicket = numeroTicket,
            numero = numero,
            para = para,
            slaPlan = slaPlan,
            dueDate = dueDate,
            ultimaMensagem = ultimaMensagem,
            ultimaResposta = ultimaResposta,
            servicos = servicos,
            campus = campus,
            sala = sala,
            bloco = bloco,
            setorSolicitante = setorSolicitante,
            nome = nome,
            msgs = msgs
        )
        return ticketDetail
    }
}