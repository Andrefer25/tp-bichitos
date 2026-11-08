package org.bichitos.desktop;

import org.bichitos.application.CsvImporter;
import org.bichitos.application.WorkflowService;
import org.bichitos.repository.DesktopQueries;
import org.bichitos.repository.DesktopQueries.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Interfaz nativa. Todas las operaciones JDBC y de archivos se ejecutan en SwingWorker. */
public final class MainFrame extends JFrame {
    final DesktopQueries queries=new DesktopQueries();
    final WorkflowService workflow=new WorkflowService();
    final long userId=1L; // Cuenta ficticia: no representa una sesión autenticada.
    final JLabel status=new JLabel("Preparado");
    final CardLayout layout=new CardLayout();
    final JPanel content=new JPanel(layout);
    final List<WorkspacePanel> views=new ArrayList<>();
    final String[] names={"Resumen","Postulaciones","Evaluaciones","Adopciones","Seguimientos","Importación"};
    final JList<String> navigation=new JList<>(names);
    final OverviewPanel overview;
    final ApplicationsPanel applications;
    final EvaluationsPanel evaluations;
    final AdoptionsPanel adoptions;
    final FollowupsPanel followups;
    final ImportPanel importing;
    volatile int pending;
    long postId;
    int current;

    public MainFrame() {
        super("Bichitos Zona Norte | Gestión local de adopciones");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(1280,880);setMinimumSize(new Dimension(1080,740));setLocationRelativeTo(null);
        UIManager.put("Table.font",new Font("Dialog",Font.PLAIN,12));
        JPanel root=new JPanel(new BorderLayout());setContentPane(root);
        JPanel header=new JPanel(new BorderLayout());header.setBackground(new Color(32,78,62));
        header.setBorder(BorderFactory.createEmptyBorder(14,20,14,20));
        JLabel brand=new JLabel("BICHITOS ZONA NORTE");brand.setForeground(Color.WHITE);brand.setFont(new Font("Dialog",Font.BOLD,21));
        header.add(brand,BorderLayout.WEST);
        JLabel local=new JLabel("Aplicación local · Java Swing · Datos ficticios");local.setForeground(Color.WHITE);header.add(local,BorderLayout.EAST);
        root.add(header,BorderLayout.NORTH);
        navigation.setFixedCellHeight(46);navigation.setFont(new Font("Dialog",Font.PLAIN,15));
        navigation.setBorder(BorderFactory.createEmptyBorder(12,10,12,10));navigation.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navigation.setBackground(new Color(232,239,232));navigation.setSelectionBackground(new Color(186,210,192));navigation.setSelectionForeground(new Color(24,65,45));
        JPanel sidebar=new JPanel(new BorderLayout());sidebar.setPreferredSize(new Dimension(180,0));sidebar.add(navigation);
        JLabel demo=new JLabel("<html>Usuario de demostración<br>Sin autenticación implementada</html>");demo.setBorder(BorderFactory.createEmptyBorder(14,10,16,10));demo.setFont(new Font("Dialog",Font.PLAIN,11));sidebar.add(demo,BorderLayout.SOUTH);
        root.add(sidebar,BorderLayout.WEST);root.add(content,BorderLayout.CENTER);
        overview=new OverviewPanel();applications=new ApplicationsPanel();evaluations=new EvaluationsPanel();
        adoptions=new AdoptionsPanel();followups=new FollowupsPanel();importing=new ImportPanel();
        Collections.addAll(views,overview,applications,evaluations,adoptions,followups,importing);
        for(int i=0;i<views.size();i++)content.add(views.get(i),names[i]);
        status.setBorder(BorderFactory.createEmptyBorder(9,16,9,16));root.add(status,BorderLayout.SOUTH);
        JPanel glass=new JPanel(new GridBagLayout());glass.setOpaque(false);glass.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        glass.addMouseListener(new java.awt.event.MouseAdapter(){});glass.addKeyListener(new java.awt.event.KeyAdapter(){});setGlassPane(glass);
        navigation.addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&navigation.getSelectedIndex()>=0){current=navigation.getSelectedIndex();layout.show(content,names[current]);views.get(current).refreshData();}});
        navigate(0);
    }
    void navigate(int index){if(navigation.getSelectedIndex()==index){layout.show(content,names[index]);views.get(index).refreshData();}else navigation.setSelectedIndex(index);}
    <T> void execute(String message,Callable<T> task,Consumer<T> success){
        if(!SwingUtilities.isEventDispatchThread())throw new IllegalStateException("Las acciones de interfaz deben iniciarse en EDT");
        pending++;status.setForeground(new Color(32,78,62));status.setText(message+"…");getGlassPane().setVisible(true);
        new SwingWorker<T,Void>() {
            protected T doInBackground()throws Exception{return task.call();}
            protected void done(){
                try{T result=get();if(isDisplayable()){status.setText("Operación completada");success.accept(result);}}
                catch(Exception e){Throwable cause=e.getCause()==null?e:e.getCause();error(cause);}
                finally{pending--;if(pending==0)getGlassPane().setVisible(false);}
            }
        }.execute();
    }
    void error(Throwable e){
        String message=e instanceof java.sql.SQLException?"No se pudo acceder a MySQL. Revisá la conexión y reintentá; los datos del formulario se conservan.":e.getMessage();
        if(message==null||message.isBlank())message="No se pudo completar la operación; revisá los datos.";
        status.setForeground(new Color(160,35,30));status.setText("Error: "+message);
    }
    JButton button(String text,Runnable action){JButton b=new JButton(text);b.addActionListener(e->{try{action.run();}catch(Exception x){error(x);}});return b;}
    JPanel heading(String title,String description){JPanel p=new JPanel(new GridLayout(0,1,0,5));p.setOpaque(false);JLabel h=new JLabel(title);h.setFont(new Font("Dialog",Font.BOLD,23));p.add(h);p.add(new JLabel(description));return p;}
    static JPanel form(){JPanel p=new JPanel(new GridBagLayout());p.setOpaque(false);return p;}
    static void field(JPanel p,int row,String label,JComponent input){GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(4,3,4,8);g.anchor=GridBagConstraints.NORTHWEST;g.gridx=0;g.gridy=row;g.weightx=0;p.add(new JLabel(label),g);g.gridx=1;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;p.add(input,g);}
    static JPanel buttons(JComponent... items){JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));p.setOpaque(false);for(var i:items)p.add(i);return p;}
    static JTextArea text(int rows){JTextArea t=new JTextArea(rows,30);t.setLineWrap(true);t.setWrapStyleWord(true);t.setFont(new Font("Dialog",Font.PLAIN,12));return t;}
    static JScrollPane scroll(JComponent c,int height){JScrollPane s=new JScrollPane(c);s.setPreferredSize(new Dimension(800,height));return s;}
    static JComboBox<String> tri(){return new JComboBox<>(new String[]{"Pendiente","Sí","No"});}
    static Boolean triValue(JComboBox<String> c){return c.getSelectedIndex()==0?null:c.getSelectedIndex()==1;}
    static void triSet(JComboBox<String> c,Boolean v){c.setSelectedIndex(v==null?0:v?1:2);}
    static JComboBox<String> choice(){return new JComboBox<>(new String[]{"Seleccionar","FAVORABLE","REVISAR","DESFAVORABLE"});}
    static String selected(JComboBox<String> c){return String.valueOf(c.getSelectedItem());}
    static LocalDate date(JTextField f){try{return LocalDate.parse(f.getText().trim());}catch(Exception e){throw new IllegalArgumentException("Ingresá una fecha válida con formato AAAA-MM-DD");}}
    void showHistory(long animalId){execute("Consultando historial",()->queries.history(animalId),g->{DataTable table=new DataTable();table.fill(g);JDialog dialog=new JDialog(this,"Historial del animal #"+animalId,false);dialog.add(new JScrollPane(table));dialog.setSize(1050,450);dialog.setLocationRelativeTo(this);dialog.setVisible(true);});}

    static final class DataTable extends JTable {
        boolean silent;
        DataTable(){setRowHeight(29);setAutoCreateRowSorter(true);setSelectionMode(ListSelectionModel.SINGLE_SELECTION);setFillsViewportHeight(true);setFont(new Font("Dialog",Font.PLAIN,12));setAutoResizeMode(JTable.AUTO_RESIZE_OFF);getTableHeader().setReorderingAllowed(false);setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){Component comp=super.getTableCellRendererComponent(t,v,s,f,r,c);setToolTipText(v==null?null:v.toString());return comp;}});}
        void fill(Grid data){long previous=id();silent=true;setModel(new DefaultTableModel(data.rows().toArray(Object[][]::new),data.columns()){public boolean isCellEditable(int r,int c){return false;}});
            for(int i=0;i<getColumnCount();i++)getColumnModel().getColumn(i).setPreferredWidth(i==0?65:150);
            if(getColumnCount()>3)getColumnModel().getColumn(getColumnCount()-1).setPreferredWidth(340);
            if(getRowCount()>0){int row=0;for(int i=0;i<getRowCount();i++)if(getModel().getValueAt(i,0) instanceof Number&&((Number)getModel().getValueAt(i,0)).longValue()==previous)row=i;int view=convertRowIndexToView(row);setRowSelectionInterval(view,view);}silent=false;}
        long id(){if(getSelectedRow()<0||getColumnCount()==0)return 0;Object v=getValueAt(getSelectedRow(),0);return v instanceof Number?((Number)v).longValue():0;}
        Object cell(int col){return getSelectedRow()<0?null:getValueAt(getSelectedRow(),col);}
        void onSelection(Runnable action){getSelectionModel().addListSelectionListener(e->{if(!silent&&!e.getValueIsAdjusting()&&getSelectedRow()>=0)action.run();});}
        void selectId(long id){for(int i=0;i<getRowCount();i++)if(getValueAt(i,0) instanceof Number&&((Number)getValueAt(i,0)).longValue()==id){setRowSelectionInterval(i,i);return;}}
    }
    final class OverviewPanel extends WorkspacePanel {
        final JLabel totals=new JLabel();final DataTable animals=new DataTable();
        OverviewPanel(){super(MainFrame.this);add(heading("Resumen","Expedientes, pendientes y animales del caso de estudio"),BorderLayout.NORTH);JPanel center=new JPanel(new BorderLayout(8,12));center.setOpaque(false);totals.setFont(new Font("Dialog",Font.BOLD,14));center.add(totals,BorderLayout.NORTH);center.add(new JScrollPane(animals));add(center);add(buttons(button("Actualizar",this::refreshData),button("Ver historial del animal",()->{if(animals.id()==0)throw new IllegalArgumentException("Seleccioná un animal");showHistory(animals.id());})),BorderLayout.SOUTH);}
        public void refreshData(){execute("Consultando resumen",()->new Object[]{queries.overview(),queries.animals()},v->{totals.setText((String)v[0]);animals.fill((Grid)v[1]);});}
    }
    final class ApplicationsPanel extends WorkspacePanel {
        final DataTable table=new DataTable();final JTextArea details=text(6);
        final JComboBox<String> identity=tri(),address=tri(),consent=tri();
        final JLabel selected=new JLabel("Seleccioná una postulación");final JButton verify;
        ApplicationInfo info;
        ApplicationsPanel(){super(MainFrame.this);add(heading("Postulaciones","Revisá datos declarados y registrá la verificación documental"),BorderLayout.NORTH);
            JPanel bottom=new JPanel(new BorderLayout(8,8));bottom.setOpaque(false);details.setEditable(false);bottom.add(selected,BorderLayout.NORTH);bottom.add(new JScrollPane(details));
            JPanel f=form();field(f,0,"Identidad comprobada",identity);field(f,1,"Domicilio comprobado",address);field(f,2,"Consentimiento comprobado",consent);
            verify=button("Guardar verificación",this::save);f.add(buttons(verify,button("Evaluar / comparar",()->{if(info==null)throw new IllegalArgumentException("Seleccioná una postulación");postId=info.id();navigate(2);})),constraints(3));
            bottom.add(f,BorderLayout.SOUTH);JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,scroll(table,235),bottom);split.setResizeWeight(.43);add(split);table.onSelection(this::load);
        }
        public void refreshData(){execute("Consultando postulaciones",queries::applications,g->{table.fill(g);load();});}
        void load(){long id=table.id();if(id==0)return;execute("Cargando expediente",()->queries.application(id),v->{info=v;postId=v.id();selected.setText("#"+v.id()+" · "+v.person()+" → "+v.animal()+" · "+v.state());details.setText(v.details());triSet(identity,v.identity());triSet(address,v.address());triSet(consent,v.consent());verify.setEnabled(v.canVerify());identity.setEnabled(v.canVerify());address.setEnabled(v.canVerify());consent.setEnabled(v.canVerify());});}
        void save(){if(info==null)throw new IllegalArgumentException("Seleccioná una postulación");long id=info.id();Boolean i=triValue(identity),a=triValue(address),c=triValue(consent);execute("Guardando verificación",()->workflow.verify(id,i,a,c,userId),v->{refreshData();status.setText("Verificación registrada: "+v.state()+". "+v.reason());});}
    }
    final class EvaluationsPanel extends WorkspacePanel {
        final DataTable candidates=new DataTable();final JLabel selectedLabel=new JLabel("Seleccioná un expediente en Postulaciones");
        final JTextArea needs=text(2),notes=text(3);final JComboBox<String> interview=choice(),home=choice(),meeting=choice();
        final JTextField interviewDate=new JTextField(LocalDate.now().toString()),homeDate=new JTextField(LocalDate.now().toString()),meetingDate=new JTextField(LocalDate.now().toString());
        final JPanel criteriaPanel=form();final Map<Long,JComboBox<String>> criterionValues=new LinkedHashMap<>();final Map<Long,JTextField> criterionNotes=new LinkedHashMap<>();
        final JComboBox<String> decisionResult=new JComboBox<>(new String[]{"APROBADA","RECHAZADA","EN_ESPERA"});final JTextField reason=new JTextField();
        final JButton save,decide;ApplicationInfo info;
        EvaluationsPanel(){super(MainFrame.this);add(heading("Evaluaciones","Comparación cualitativa por animal; la decisión siempre es humana"),BorderLayout.NORTH);
            JPanel body=new JPanel();body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));body.setOpaque(false);body.add(selectedLabel);needs.setEditable(false);body.add(scroll(needs,55));body.add(scroll(candidates,130));
            JPanel f=form();field(f,0,"Entrevista y fecha",buttons(interview,interviewDate));field(f,1,"Vivienda y fecha",buttons(home,homeDate));field(f,2,"Encuentro y fecha",buttons(meeting,meetingDate));field(f,3,"Observaciones",new JScrollPane(notes));body.add(f);body.add(criteriaPanel);
            save=button("Guardar evaluación",this::save);body.add(buttons(save,button("Historial",()->{if(info!=null)showHistory(info.animalId());})));
            JPanel decision=form();decision.setBorder(BorderFactory.createTitledBorder("Decisión fundada"));field(decision,0,"Resultado",decisionResult);field(decision,1,"Fundamento",reason);decide=button("Registrar decisión",this::decide);decision.add(buttons(decide),constraints(2));body.add(decision);
            add(new JScrollPane(body));candidates.onSelection(()->{postId=candidates.id();load();});
        }
        public void refreshData(){if(postId==0){execute("Buscando expediente",queries::applications,g->{if(!g.rows().isEmpty()){postId=((Number)g.rows().get(0)[0]).longValue();load();}});}else load();}
        void load(){long id=postId;execute("Consultando evaluación",()->{ApplicationInfo p=queries.application(id);return new Object[]{p,queries.evaluation(id,p.animalId()),queries.comparison(p.animalId())};},v->{
            info=(ApplicationInfo)v[0];Evaluation e=(Evaluation)v[1];selectedLabel.setText("#"+info.id()+" · "+info.person()+" → "+info.animal()+" · "+info.state());needs.setText("Necesidades: "+info.needs());candidates.fill((Grid)v[2]);candidates.silent=true;candidates.selectId(info.id());candidates.silent=false;
            interview.setSelectedItem(e.interview().isEmpty()?"Seleccionar":e.interview());home.setSelectedItem(e.home().isEmpty()?"Seleccionar":e.home());meeting.setSelectedItem(e.meeting().isEmpty()?"Seleccionar":e.meeting());notes.setText(e.notes());
            interviewDate.setText(e.dates().getOrDefault("ENTREVISTA",LocalDate.now()).toString());homeDate.setText(e.dates().getOrDefault("VIVIENDA",LocalDate.now()).toString());meetingDate.setText(e.dates().getOrDefault("ENCUENTRO",LocalDate.now()).toString());
            criterionValues.clear();criterionNotes.clear();criteriaPanel.removeAll();int row=0;
            for(Criterion c:e.criteria()){JComboBox<String> values=new JComboBox<>(new String[]{"Seleccionar","FAVORABLE","REVISAR","DESFAVORABLE","NO_APLICA"});values.setSelectedItem(c.value().isEmpty()?"Seleccionar":c.value());JTextField cn=new JTextField(c.notes(),30);criterionValues.put(c.id(),values);criterionNotes.put(c.id(),cn);field(criteriaPanel,row++,c.name(),buttons(values,cn));values.setEnabled(info.canEvaluate());cn.setEditable(info.canEvaluate());}
            for(JComponent c:new JComponent[]{interview,home,meeting,interviewDate,homeDate,meetingDate,notes})c.setEnabled(info.canEvaluate());save.setEnabled(info.canEvaluate());decide.setEnabled(info.canDecide());decisionResult.setEnabled(info.canDecide());reason.setEditable(info.canDecide());reason.setText("");criteriaPanel.revalidate();criteriaPanel.repaint();});}
        void save(){if(info==null)throw new IllegalArgumentException("Seleccioná una postulación");long id=info.id();Map<Long,WorkflowService.CriterionValue> values=new LinkedHashMap<>();for(long c:criterionValues.keySet())values.put(c,new WorkflowService.CriterionValue(selected(criterionValues.get(c)),criterionNotes.get(c).getText()));
            Map<String,LocalDate> dates=Map.of("ENTREVISTA",date(interviewDate),"VIVIENDA",date(homeDate),"ENCUENTRO",date(meetingDate));String i=selected(interview),h=selected(home),m=selected(meeting),n=notes.getText();execute("Guardando evaluación",()->workflow.evaluate(id,i,h,m,n,values,dates,userId),v->{load();status.setText("Evaluación registrada para la postulación #"+id);});}
        void decide(){if(info==null)throw new IllegalArgumentException("Seleccioná una postulación");long id=info.id();String r=selected(decisionResult),why=reason.getText();execute("Registrando decisión",()->workflow.decide(id,r,why,userId),v->{load();status.setText("Decisión #"+v+" registrada; la entrega se registra en Adopciones.");});}
    }
    final class AdoptionsPanel extends WorkspacePanel {
        final DataTable table=new DataTable();final JTextField delivery=new JTextField(LocalDate.now().toString());final JCheckBox signed=new JCheckBox("Compromiso firmado comprobado");final JButton adopt;
        AdoptionsPanel(){super(MainFrame.this);add(heading("Adopciones","Formalizá la entrega después de una decisión aprobada"),BorderLayout.NORTH);add(new JScrollPane(table));JPanel f=form();field(f,0,"Fecha de entrega AAAA-MM-DD",delivery);field(f,1,"Compromiso",signed);adopt=button("Registrar entrega y generar agenda",this::save);f.add(buttons(adopt,button("Ver seguimientos",()->navigate(4))),constraints(2));add(f,BorderLayout.SOUTH);table.onSelection(this::selection);}
        public void refreshData(){execute("Consultando adopciones",queries::decisions,g->{table.fill(g);selection();});}
        void selection(){adopt.setEnabled(table.id()>0&&"APROBADA".equals(table.cell(4))&&table.cell(6)==null);}
        void save(){long id=table.id();if(id==0)throw new IllegalArgumentException("Seleccioná una decisión");LocalDate d=date(delivery);boolean s=signed.isSelected();execute("Registrando entrega",()->workflow.adopt(id,d,s,userId),v->{refreshData();status.setText("Adopción #"+v+" registrada con seguimientos a los 2–3, 15 y 30 días.");});}
    }
    final class FollowupsPanel extends WorkspacePanel {
        final DataTable tasks=new DataTable(),alerts=new DataTable();final JTextField channel=new JTextField(),action=new JTextField();final JTextArea notes=text(3);
        final JComboBox<String> result=new JComboBox<>(new String[]{"SATISFACTORIO","ACOMPANAMIENTO","ALERTA","POSIBLE_DEVOLUCION","SIN_RESPUESTA"});final JLabel chosen=new JLabel();final JButton save,close;
        FollowupsPanel(){super(MainFrame.this);add(heading("Seguimientos","Registrá contactos, atendé vencidos y resolvé acciones de alerta"),BorderLayout.NORTH);
            JPanel body=new JPanel();body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));body.add(scroll(tasks,175));body.add(chosen);JPanel f=form();field(f,0,"Canal",channel);field(f,1,"Resultado",result);field(f,2,"Observaciones",new JScrollPane(notes));body.add(f);save=button("Registrar contacto",this::save);body.add(buttons(save));body.add(new JLabel("Acciones de alerta"));body.add(scroll(alerts,125));JPanel a=form();field(a,0,"Acción realizada",action);close=button("Cerrar acción de alerta",this::close);a.add(buttons(close),constraints(1));body.add(a);add(new JScrollPane(body));tasks.onSelection(this::selection);alerts.onSelection(()->close.setEnabled("ABIERTA".equals(alerts.cell(4))));}
        public void refreshData(){execute("Consultando agenda",()->new Grid[]{queries.followups(),queries.alerts()},g->{tasks.fill(g[0]);alerts.fill(g[1]);selection();close.setEnabled(alerts.id()>0&&"ABIERTA".equals(alerts.cell(4)));});}
        void selection(){boolean open=tasks.id()>0&&tasks.cell(7)==null;save.setEnabled(open);channel.setEditable(open);notes.setEditable(open);result.setEnabled(open);chosen.setText(tasks.id()==0?"Sin tareas":"Seguimiento #"+tasks.id()+" · "+tasks.cell(2)+" · "+tasks.cell(3)+" · "+tasks.cell(11));channel.setText(tasks.cell(9)==null?"":tasks.cell(9).toString());notes.setText(tasks.cell(10)==null?"":tasks.cell(10).toString());if(tasks.cell(8)!=null)result.setSelectedItem(tasks.cell(8));else result.setSelectedIndex(0);}
        void save(){long id=tasks.id();if(id==0)throw new IllegalArgumentException("Seleccioná un seguimiento");String c=channel.getText(),r=selected(result),n=notes.getText();execute("Guardando contacto",()->{Long owner=r.equals("ALERTA")||r.equals("POSIBLE_DEVOLUCION")?queries.coordinator():null;workflow.followUp(id,c,r,n,owner,userId);return id;},v->{refreshData();status.setText("Contacto registrado. Revisá la agenda y las acciones abiertas.");});}
        void close(){long id=alerts.id();String a=action.getText();if(id==0)throw new IllegalArgumentException("Seleccioná una alerta");execute("Cerrando acción",()->{workflow.closeAlert(id,a,userId);return id;},v->{action.setText("");refreshData();});}
    }
    final class ImportPanel extends WorkspacePanel {
        final JTextField file=new JTextField();final JLabel summary=new JLabel("Seleccioná un CSV exportado con la estructura del proyecto");final DataTable batches=new DataTable(),errors=new DataTable();
        ImportPanel(){super(MainFrame.this);add(heading("Importación","Ingreso manual por CSV; los errores de una fila no detienen las demás"),BorderLayout.NORTH);file.setEditable(false);JPanel body=new JPanel();body.setLayout(new BoxLayout(body,BoxLayout.Y_AXIS));JPanel choose=new JPanel(new BorderLayout(8,8));choose.setMaximumSize(new Dimension(Integer.MAX_VALUE,40));choose.add(file);choose.add(button("Elegir CSV",()->{JFileChooser picker=new JFileChooser(new File("data"));picker.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivo CSV UTF-8","csv"));if(picker.showOpenDialog(MainFrame.this)==JFileChooser.APPROVE_OPTION)file.setText(picker.getSelectedFile().getAbsolutePath());}),BorderLayout.EAST);body.add(choose);body.add(buttons(button("Importar archivo",this::save)));body.add(summary);body.add(scroll(batches,230));body.add(new JLabel("Errores del lote seleccionado · corregir en el archivo de origen"));body.add(scroll(errors,200));add(body);batches.onSelection(this::loadErrors);}
        public void refreshData(){execute("Consultando lotes",queries::batches,g->{batches.fill(g);loadErrors();});}
        void loadErrors(){long id=batches.id();if(id>0)execute("Consultando errores",()->queries.batchErrors(id),errors::fill);}
        void save(){Path path;try{path=Path.of(file.getText());}catch(Exception e){throw new IllegalArgumentException("Seleccioná un archivo CSV");}if(file.getText().isBlank()||!file.getText().toLowerCase(Locale.ROOT).endsWith(".csv"))throw new IllegalArgumentException("Seleccioná un archivo .csv");
            execute("Importando CSV",()->new CsvImporter().importFile(path,userId),v->{summary.setText("Lote #"+v.batchId()+": "+v.incorporated()+" incorporadas · "+v.duplicates()+" duplicadas · "+v.failed()+" rechazadas");refreshData();});}
    }
    static GridBagConstraints constraints(int row){GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.gridy=row;g.gridwidth=2;g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;return g;}
}
